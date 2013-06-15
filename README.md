ampache-lib
===========

A set of Clojure libraries for interacting with ampache music server through it's database

## Installation

leiningen

    [com.madeye.clojure.ampache/ampachedb "0.1.0"]

maven

    <dependency>
      <groupId>com.madeye.clojure.ampache</groupId>
      <artifactId>ampachedb</artifactId>
      <version>0.1.0</version>
    </dependency>

## Usage

This is a library so direct usage is not possible

## Examples

    com.madeye.clojure.ampache.ampachedb=> (initialise "config.properties")
    "Database initialised from config.properties"

    com.madeye.clojure.ampache.ampachedb=> (find-song "Feeling Good")
    [{:enabled true, :artist 170, :rate 44100, :year 1965, :addition_time 1302422729, :title "Feeling Good", :size 3461907, :bitrate 160000, :update_time 0, :album 232, :played true, :track 3, :time 172, :mode "cbr", :id 2469, :file "/mnt/brahma/subsonic/music/Nina Simone/The Very Best Of Nina Simone/03 - Feeling Good.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 170, :rate 44100, :year 0, :addition_time 1302438289, :title "Feeling Good", :size 2790490, :bitrate 128000, :update_time 0, :album 875, :played true, :track 0, :time 174, :mode "cbr", :id 10124, :file "/mnt/brahma/subsonic/music/Nina Simone/Nina Simone/ - Feeling Good.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 182, :rate 44100, :year 2001, :addition_time 1302438319, :title "Feeling Good", :size 4798090, :bitrate 192000, :update_time 0, :album 914, :played false, :track 0, :time 199, :mode "cbr", :id 10527, :file "/mnt/brahma/subsonic/music/Muse/Origin Of Symmetry (advance)/ - Feeling Good.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 182, :rate 44100, :year 0, :addition_time 1302438320, :title "Feeling good", :size 3227885, :bitrate 128000, :update_time 0, :album 915, :played false, :track 10, :time 199, :mode "cbr", :id 10549, :file "/mnt/brahma/subsonic/music/Muse/Origin of symmetry/10 - Feeling good.mp3", :mbid nil, :catalog 1}]

    com.madeye.clojure.ampache.ampachedb=> (find-song "Feeling Good" "Origin of Symmetry")
    [{:enabled true, :artist 182, :prefix nil, :name "Origin of symmetry", :rate 44100, :id_2 915, :year 0, :year_2 0, :addition_time 1302438320, :title "Feeling good", :size 3227885, :bitrate 128000, :update_time 0, :album 915, :disk 0, :played false, :track 10, :time 199, :mode "cbr", :mbid_2 nil, :id 10549, :file "/mnt/brahma/subsonic/music/Muse/Origin of symmetry/10 - Feeling good.mp3", :mbid nil, :catalog 1}]

    com.madeye.clojure.ampache.ampachedb=> (find-song "Feeling Good" "Origin of Symmetry" "Muse")
    [{:enabled true, :artist 182, :prefix_2 nil, :prefix nil, :name "Origin of symmetry", :rate 44100, :id_2 915, :id_3 182, :year 0, :year_2 0, :addition_time 1302438320, :title "Feeling good", :size 3227885, :bitrate 128000, :update_time 0, :album 915, :disk 0, :played false, :track 10, :time 199, :mode "cbr", :mbid_2 nil, :id 10549, :file "/mnt/brahma/subsonic/music/Muse/Origin of symmetry/10 - Feeling good.mp3", :mbid nil, :name_2 "Muse", :catalog 1, :mbid_3 nil}]

    com.madeye.clojure.ampache.ampachedb=> (find-artist-by-name "Jake Bugg")
    [{:mbid nil, :prefix nil, :name "Jake Bugg", :id 1382}]

    com.madeye.clojure.ampache.ampachedb=> (find-album-by-name "Iron Maiden")
    ({:song [{:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423059, :title "Phantom of the Opera", :size 17185714, :bitrate 320000, :update_time 0, :album 517, :played true, :track 5, :time 427, :mode "cbr", :id 6163, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/05 - Phantom of the Opera.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423059, :title "Iron Maiden", :size 8647864, :bitrate 320000, :update_time 0, :album 517, :played true, :track 9, :time 214, :mode "cbr", :id 6164, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/09 - Iron Maiden.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423059, :title "Prowler", :size 9505538, :bitrate 320000, :update_time 0, :album 517, :played true, :track 1, :time 235, :mode "cbr", :id 6165, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/01 - Prowler.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423059, :title "Strange World", :size 13366748, :bitrate 320000, :update_time 0, :album 517, :played true, :track 7, :time 332, :mode "cbr", :id 6166, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/07 - Strange World.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423059, :title "Charlotte the Harlot", :size 10166902, :bitrate 320000, :update_time 0, :album 517, :played true, :track 8, :time 252, :mode "cbr", :id 6167, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/08 - Charlotte the Harlot.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423059, :title "Sanctuary", :size 7921288, :bitrate 320000, :update_time 0, :album 517, :played true, :track 2, :time 196, :mode "cbr", :id 6168, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/02 - Sanctuary.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423060, :title "Remember Tomorrow", :size 13196610, :bitrate 320000, :update_time 0, :album 517, :played true, :track 3, :time 328, :mode "cbr", :id 6169, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/03 - Remember Tomorrow.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423060, :title "Running Free", :size 7951544, :bitrate 320000, :update_time 0, :album 517, :played true, :track 4, :time 196, :mode "cbr", :id 6170, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/04 - Running Free.mp3", :mbid nil, :catalog 1} {:enabled true, :artist 345, :rate 44100, :year 2002, :addition_time 1302423060, :title "Transylvania", :size 10422666, :bitrate 320000, :update_time 0, :album 517, :played true, :track 6, :time 258, :mode "cbr", :id 6171, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/06 - Transylvania.mp3", :mbid nil, :catalog 1}], :disk 0, :year 2002, :mbid nil, :prefix nil, :name "Iron Maiden", :id 517} {:song [{:enabled true, :artist 345, :rate 44100, :year 1980, :addition_time 1302438523, :title "Prowler", :size 3759180, :bitrate 128000, :update_time 0, :album 1147, :played false, :track 0, :time 234, :mode "cbr", :id 13263, :file "/mnt/brahma/subsonic/music/Iron Maiden/Iron Maiden/ - Prowler.mp3", :mbid nil, :catalog 1}], :disk 0, :year 1980, :mbid nil, :prefix nil, :name "Iron Maiden", :id 1147})


### Bugs

None identified so far

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
