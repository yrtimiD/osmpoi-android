OSM tags always consists of key and value, and each tag usually noted as key=value

Application allows to manually compose more complex searches by using additional operators:

| | | logical or |
|:--|:-----------|
| & | logical and |
| ! | negation |
| ( ) | logical parts separation |
| `*` | wildcard character |
Example:

search for free parking:
```
amenity=parking & fee=no
```

search for ATM:
```
(amenity=bank & atm=yes) | (amenity=atm)
```

search for any shop:
```
shop=*
```


Note:
_keys may not have spaces, but values can freely contain spaces_<br>


List of all possible (and not possible) keys and values can be seen at <a href='http://wiki.openstreetmap.org/wiki/Map_Features'>http://wiki.openstreetmap.org/wiki/Map_Features</a>