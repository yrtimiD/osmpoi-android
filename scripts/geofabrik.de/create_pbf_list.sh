wget -r -L -e robots=off -A html http://download.geofabrik.de/osm/
grep -r "osm.pbf" *|sort >rows
perl -n -e'/([^:]+)index.+a href="([^"]+)/ && print "http://$1$2\n"' rows >pbf_list

