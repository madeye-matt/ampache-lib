(ns com.madeye.clojure.ampache.ampachedb
  (:gen-class))

(use 'korma.core)
(require '[clojure.string :as str])
(require '[com.madeye.clojure.common.common :as c])

(defn reload [] (use :reload-all 'com.madeye.clojure.ampache.ampachedb))

; Declaring vars that will be initialised by (initialise)
(declare config database-config db)
(declare artist album song user object_count)

(defn initialise
  "Initialisation function accepting the name of a properties file containing the database configuration - must be called before calling any of the other functions"
  [config-file]
  (def config (c/load-props config-file))
  (def database-config {
        :host (config :database-host)
        :port (config :database-port)
        :db (config :database-db)
        :user (config :database-user)
        :password (config :database-password) }
  )
  (korma.db/defdb db
    (korma.db/mysql database-config)) 

  (defentity artist
      (pk :id)
      (table :artist)
      (database db)
      (entity-fields :name :prefix)
      (has-many song { :fk :artist })

  )
      
  (defentity album
      (pk :id)
      (table :album)
      (database db)
      (entity-fields :name :prefix :year :disc)
      (has-many song { :fk :album })
  )

  (defentity song
      (pk :id)
      (table :song)
      (database db)
      (entity-fields :title :artist :album :track :catalog :year)
      (belongs-to album { :fk :album })
      (belongs-to artist { :fk :artist })
  )

  (defentity object_count
      (pk :id)
      (table :object_count)
      (database db)
      (entity-fields :object_type :object_id :date :user)
      (belongs-to user { :fk :user })
  )

  (defentity user
      (pk :id)
      (table :user)
      (database db)
      (entity-fields :username :fullname :email :password :access :disabled :last_seen :create_date :validation)
      (has-many song { :fk :object_count })
  )

  (format "Database initialised from %s" config-file)
)

; WHat follows is a bunch of complexity around prefixes.  Ampache has a list of words (A, The etc) that it deems as prefixes.  In the representations
; of artists and albums the names (e.g. The Rolling Stones) are split into two components, prefix (e.g. The) and name (e.g. Rolling Stones).  So
; any incoming function calls for artists or albums that begin with one of these prefixes require the name to be split before any queries are
; submitted to the database.  Hence the next 20 lines or so

; Valid prefixes as defined by Ampache - not canonical list
(def prefixes ["A" "An" "The" "La" "Le" "Der" "Die" "Das"])

; Regexp for parsing valid prefixes
(def prefix-regexp (re-pattern (str "(" (reduce #(str %1 "|" %2) prefixes) ")\\s(.*)|.*") ))

;(defn- full-name
;  "Convenience function to get the full name for an artist or album - prefix + name"
;  [m] (str/trim (str (:prefix m) " " (:name m))))

(defn- prepend-keyword [m prefix] (into {} (for [[k v] m] [(keyword (str prefix "." (name k))) v])))

(defn- name-map 
  "Function to create a :name and optionally :prefix map from the result of using re-find with above regexp"  
  [v] (if (= nil (v 1)) { :name (v 0) } { :prefix (v 1) :name (v 2) }))


(defn- split-name-string 
  "Function to take album name (string) and create a :name/:prefix map"
  [albumname] (name-map (re-find prefix-regexp albumname))) 

(defn- build-join-map [type name] (prepend-keyword (split-name-string name) type))

; Function to take map with :album tag and create a :name/prefix map
;(defn- split-name-map [m] (split-name-string (:album m)))

; Function to conj :name/:prefix with original map 
;(defn- merge-name-prefix [m] (conj m (split-name-map m)))

(defn find-artist 
  "Find an artist with given a map with 'where' parameters"
  [m] (select artist (where m)))

(defn find-artist-by-name 
  "Find an artist with a given name"
  [name] (find-artist (split-name-string name)))

(defn find-artist-by-id 
  "Find an artist with a given id"
  [id] (find-artist { :id id }))

(defn- get-artist-name
  "Ampache has quite a bad habit of just dropping the prefix from artist names - so The Rolling Stones becomes just Rolling Stones. This function attempts to guess the artist name by trying the whole name, then trying the name with the prefix, if any, dropped"
  [s]
  (let [a (find-artist-by-name s)]
    (if (empty? a)
      (let [newresult (find-artist-by-name (:name (split-name-string s)))]
        (if (empty? newresult)
          s
          (:name (first newresult)) 
        )
      )
      (:name (first a))
    )
  )
)

(defn find-album 
  "Find an album with given a map with 'where' parameters"
  [m] (select album (with song) (where m)))
(defn find-album-by-name 
  "Find an album with a given name"
  [name] (find-album (split-name-string name)))
(defn find-album-by-id 
  "Find an album with a given id"
  [id] (find-album {:id id}))

(defn find-user 
  "Find an Ampache user with given a map with 'where' parameters"
  [m] (select user (where m)))
(defn find-user-by-username 
  "Find an Ampache user with a given name"
  [username] (first (find-user { :username username })))
(defn find-user-by-id 
  "Find an Ampache user with a given userid"
  [id] (first (find-user {:id id})))

(defn find-song 
    "Function to find a song supplying the song name, plus optional album name, plus optional artist name - please note, if artist name is supplied then album name is not optional"
    ([title] (select song (where {:title title})))
    ([title albump] (select song (with album) (where (assoc (build-join-map "album" albump) :title title))))
    ([title albump artistp] (select song (with album) (with artist) (where (assoc (conj (build-join-map "album" albump) (build-join-map "artist" (get-artist-name artistp))) :title title ))))
)

(defn find-song-by-map
    "Utility function to pull the parameters to find-song from a map with the keywords :song, :album, :artist"
    [m]
    (find-song (:song m) (:album m) (:artist m))
)

(defn song-listened 
    "Create a record of a song being listened to at a particular time"
    [songp albump artistp timep userp]
    (insert object_count (values { :object_type "artist" :object_id artistp :date timep :user userp }))
    (insert object_count (values { :object_type "album" :object_id albump :date timep :user userp }))
    (insert object_count (values { :object_type "song" :object_id songp :date timep :user userp }))
)

(defn song-listened?
    "Has a song been listened to at a particular time?"
    [songp albump artistp timep userp]
    (and 
        (not (empty? (select object_count (where { :object_id songp :object_type "song" :date timep :user userp }))))
        (not (empty? (select object_count (where { :object_id albump :object_type "album" :date timep :user userp }))))
        (not (empty? (select object_count (where { :object_id artistp :object_type "artist" :date timep :user userp }))))
    )
)

