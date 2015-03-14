# Introduction #

Result formatters it's like styles which describes how to show specific result item


# Details #

The configuration saved in XML format.

[Here](http://code.google.com/p/osmpoi-android/source/browse/assets/entity_formatters.xml) is the most updated version.

The rules matched from top to bottom, the first matched - wins.

The "match" attribute defines tags matching query, if any of entity tags matches the query - current formatter used to format result row for this entity. Query format is the same as in [Custom Search](CustomSearch.md) feature. If operator AND used within XML it must be escaped as `&amp;`.

The "select\_pattern" attribute is a result format string. All elements within { } (curly braces) will be substituted with the corresponding tag values. If key was not found - the whole place holder will be removed.

Application supports localized tags. For example, if select\_pattern have "name" tag, and "Preferred result language" is Russian, then result will be values of the tags in next order:
  1. name:ru
  1. name
  1. name:en _(English is the default fall-back language)_


