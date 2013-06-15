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

### Bugs

None identified so far

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
