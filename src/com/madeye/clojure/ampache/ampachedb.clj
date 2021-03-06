(ns com.madeye.clojure.ampache.ampachedb
  (:gen-class))

(use 'korma.core)
(require '[clojure.string :as str])
(require '[clojure.xml :as xml])
(require '[clj-time.core :as tm])
(require '[clj-time.local :as tloc])
(require '[clj-time.format :as tfmt])
(require '[com.madeye.clojure.common.common :as c])

(defn reload [] (use :reload-all 'com.madeye.clojure.ampache.ampachedb))

; Declaring vars that will be initialised by (initialise)
(declare config database-config db default-top)
(declare artist album song user object_count)

(def date-formatter (tfmt/formatter "yyyy-MM-dd HH:mm:ss"))

(defn- add-timestamp
  "Adds a :timestamp entry to the map containing the formatted version of the :date"
  [m]
  (assoc m :timestamp (tfmt/unparse date-formatter (c/from-unix-time (:date m))))
)

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
  (def default-top (read-string (config :default-top)))
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
      (transform (fn [m] (add-timestamp m)))
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

(defn- prepend-keyword [m prefix] (into {} (for [[k v] m] [(keyword (str prefix "." (name k))) v])))

(defn- name-map 
  "Function to create a :name and optionally :prefix map from the result of using re-find with above regexp"  
  [v] (if (= nil (v 1)) { :name (v 0) } { :prefix (v 1) :name (v 2) }))


(defn- split-name-string 
  "Function to take album name (string) and create a :name/:prefix map"
  [albumname] (name-map (re-find prefix-regexp albumname))) 

(defn- build-join-map [type name] (prepend-keyword (split-name-string name) type))

(defn find-artist 
  "Find an artist with given a map with 'where' parameters"
  ([m min-fields] 
    (if min-fields
      (select artist (fields :prefix :name :id) (where m))
      (select artist (where m))
    )
  )
  ([m] (find-artist m false))
)

(defn find-artist-by-name 
  "Find an artist with a given name"
  ([name] (find-artist-by-name name false))
  ([name min-fields] (find-artist (split-name-string name) min-fields))
)

(defn find-artist-by-id 
  "Find an artist with a given id"
  ([id min-fields] (find-artist { :id id } min-fields))
  ([id] (find-artist-by-id id false))
)

(defn- guess-artist-name
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
  ([m min-fields] 
    (if min-fields
      (select album (fields :prefix :name :id ) (where m))
      (select album (where m))
    ))
  ([m] (find-album m false))
)
(defn find-album-by-name 
  "Find an album with a given name"
  ([name] (find-album-by-name name false))
  ([name min-fields] (find-album (split-name-string name) min-fields))
)
(defn find-album-by-id 
  "Find an album with a given id"
  ([id min-fields] (find-album {:id id} min-fields))
  ([id] (find-album-by-id id false))
)

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
    ([title albump artistp] (select song (with album) (with artist) (where (assoc (conj (build-join-map "album" albump) (build-join-map "artist" (guess-artist-name artistp))) :title title ))))
)
(defn find-song-by-map
    "Utility function to pull the parameters to find-song from a map with the keywords :song, :album, :artist"
    ([m] (find-song-by-map m false))
    ([m min-fields] 
      (if min-fields
        (select song (fields :title :artist :album :id) (where m))
        (select song (where m))
      )
    )
)

(defn find-song-by-id 
  "Find a song with a given id"
  ([id] (find-song-by-id id false))
  ([id min-fields] (find-song-by-map {:id id} min-fields))
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

(defn find-song-listen
  "Function to list songs listened to over particular time period"
  ([filters]
  (let [ustart (c/to-unix-time (:start filters))
        uend (c/to-unix-time (:end filters))
        filters (dissoc filters :start :end)
        basequery (-> (select* object_count)
            (fields [:song.title :song] [:artist.name :artist] [:album.name :album] :date [:user.fullname :user] [:song.id :songid] [:artist.id :artistid] [:album.id :albumid])
            (join song (= :song.id :object_id))
            (join artist (= :artist.id :song.artist))
            (join album (= :album.id :song.album))
            (join user (= :user.id :user))
            (where { :object_type "song" } )
            (where { :date [> ustart ] })
            (where { :date [< uend ] })
        )
        ]
        (if (empty? filters)
          (-> basequery
            (select)
          )
          (-> basequery
            (where filters)
            (select)
          )
        )
  ))
)

(defn group-map [idfn namefn m] { :name (namefn m) :id (idfn m) :type namefn})

; A set of functions for creating group maps
(def group-map-artist (partial group-map :artistid :artist))
(def group-map-album (partial group-map :albumid :album))
(def group-map-song (partial group-map :songid :song))

; A set of partial functions for grouping by various things
(def group-artist (partial group-by group-map-artist))
(def group-album (partial group-by group-map-album))
(def group-song (partial group-by group-map-song))
(def group-timestamp (partial group-by :timestamp))
(def group-user (partial group-by :user))

(defn top
    "Returns the top n of the supplied plays using the specified group-function (group-artist etc)"
    ([m fn num] (take num (sort c/compare-count-map-desc (map c/count-record (fn m )))))
    ([m fn] (top m fn default-top))
    ([m] (top m group-artist))
)

(defn- get-name
  "Gets the name of an album or artist - merging the prefix field with the name if necessary"
  [m]
  (if-let [prefix (:prefix m)]
    (str prefix " " (:name m))
    (:name m)
  )
)

(defn- replace-prefix-with-full-name
 "Replaces the :prefix/:name combo with a single :name entry that contains the full name"
 [m]
 (dissoc (assoc m :name (get-name m)) :prefix)
)

(defn- replace-unix-time
  "Replaces Unix time in :date field with formatted time in :timestamp field"
  [m]
  (dissoc (add-timestamp m) :date)
)

(defn find-object 
  "Accepts a map with :id and :type and returns a map representing the specified artist, album or song"
  ([m min-fields] (case (:type m) 
        :artist 
          (replace-prefix-with-full-name (conj (first (find-artist-by-id (:id m) min-fields)) { :type :artist }))
        :album 
          (replace-prefix-with-full-name (conj (first (find-album-by-id (:id m) min-fields)) { :type :album }))
        :song 
          (conj (first (find-song-by-id (:id m) min-fields)) { :type :song })
      )
  )
  ([m] (find-object m false))
)

(defn transform-top-result 
  "Function to transform the map returned from 'top' into a map containing metadata about the objects with the count merged in"
  ([topmap] (transform-top-result topmap false))
  ; ([min-fields topmap] (replace-unix-time (conj (find-object (:term topmap) min-fields) { :count (:count topmap)} )))
  ([min-fields topmap] (conj (find-object (:term topmap) min-fields) { :count (:count topmap)} ))
)

(def transform-top-result-min-fields (partial transform-top-result true))

(defn top-result 
  "Function to deal with all aspects of 'top' functionality"
  ([filters groupfn numrecs] (map transform-top-result-min-fields (top (find-song-listen filters) groupfn numrecs)))
)
