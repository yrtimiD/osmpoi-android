# Introduction #

The OsmPoi-DbCreator tool is intended to simplify and speed up the process of converting standard .PBF files to the format usable by OsmPoi application.


# How to run #
Application - is a regular java jar file, so java must be installed on the computer.

The zip archive contains two scripts: dbcreator.bat and dbcreator.sh for windows and `*`nix.

When application executed without parameters - list of available switches is printed:
```
--create <pbf file name>	create new DB from PBF file
--save-grid <db file>	        outputs grid from DB in poly format (dev option)
--rebuild-grid <db file>	rebuilds grid (dev option)
```

### --create ###
Produces set of new databases from supplied .PBF file
This argument must be followed by path to .PBF file, and after some work in current directory two files will be produced (overwritten if exists before):
```
poi.db
addr.db
```
these two files must be copied to the Android device into folder:
```
/sdcard/Android/data/il.yrtimid.osm.osmpoi/files/
```
_Path may vary depending on the manufacturer_

### --save-grid and --rebuild-grid ###
Are both developing options and may not be useful for the end user.