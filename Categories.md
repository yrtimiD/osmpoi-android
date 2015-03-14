# Introduction #
Categories, is the hierarchical list of search topics

# Details #
Categories may be of three types:
  * Taxonomy
  * Search
  * Inline

**Taxonomy** - it's a simple folder for other categories

**Search** - is the specific search starting point, clicking on which will lead to starting search with the query defined in "`query`" attribute.

**Inline** - is a dynamic category list, once opened it will be populated with distinct results accordingly to "`query`" and "`select`" attributes. For example, it's useful for prefetching shop of fuel stations brands.

# Current categories #

  * emergency `()`
    * hospital `(amenity=hospital)`
    * clinic `(amenity=clinic)`
    * doctors `(amenity=doctors)`
    * ambulance\_station `(emergency=ambulance_station)`
    * emergency\_phone `(emergency=phone)`
    * emergency\_bay `(highway=emergency_bay)`
    * police `(amenity=police)`
    * shelter `(amenity=shelter)`
  * transport `()`
    * bus\_stop `(highway=bus_stop)`
    * bus\_station `(amenity=bus_station | public_transport=station)`
    * railway\_station `(railway=station | building=train_station)`
    * airport `(aeroway=aerodrome)`
    * parking `(amenity=parking | amenity=parking_entrance)`
    * fuel `(amenity=fuel)`
    * fuel\_operator `(amenity=fuel)`
    * car\_wash `(amenity=car_wash)`
    * car\_rental `(amenity=car_rental)`
    * car\_rental\_operator `(amenity=car_rental & operator=*)`
    * compressed\_air `(amenity=compressed_air)`
    * taxi `(amenity=taxi)`
    * bicycle\_rental `(amenity=bicycle_rental)`
    * bicycle\_parking `(amenity=bicycle_parking)`
  * finance `()`
    * bank `(amenity=bank)`
    * atm `(amenity=atm | atm=yes)`
    * bureau\_de\_change `(amenity=bureau_de_change)`
  * shopping `()`
    * all\_in\_this\_category `(shop=*)`
    * supermarket `(shop=supermarket)`
    * mall `(shop=mall)`
    * pharmacy `(amenity=pharmacy)`
    * alcohol `(amenity=alcohol)`
    * anime `(amenity=anime)`
    * appliance `(amenity=appliance)`
    * art `(amenity=art)`
    * baby\_goods `(amenity=baby_goods)`
    * bakery `(amenity=bakery)`
    * bathroom\_furnishing `(amenity=bathroom_furnishing)`
    * beauty `(amenity=beauty)`
    * bed `(amenity=bed)`
    * beverage `(amenity=beverage)`
    * bycicle `(amenity=bycicle)`
    * books `(amenity=books)`
    * boutique `(amenity=boutique)`
    * butcher `(amenity=butcher)`
    * car `(amenity=car)`
    * car\_repair `(amenity=car_repair)`
    * carpet `(amenity=carpet)`
    * charity `(amenity=charity)`
    * chemist `(amenity=chemist)`
    * clothes `(amenity=clothes)`
    * computer `(amenity=computer)`
    * confectionery `(amenity=confectionery)`
    * convenience `(amenity=convenience)`
    * copyshop `(amenity=copyshop)`
    * curtain `(amenity=curtain)`
    * deli `(amenity=deli)`
    * department\_store `(amenity=department_store)`
    * dive `(amenity=dive)`
    * doityourself `(amenity=doityourself)`
    * drugstore `(amenity=drugstore)`
    * dry\_cleaning `(amenity=dry_cleaning)`
    * electronics `(amenity=electronics)`
    * erotic `(amenity=erotic)`
    * fabric `(amenity=fabric)`
    * farm `(amenity=farm)`
    * florist `(amenity=florist)`
    * frame `(amenity=frame)`
    * funeral\_directors `(amenity=funeral_directors)`
    * furnace `(amenity=furnace)`
    * furniture `(amenity=furniture)`
    * garden\_centre `(amenity=garden_centre)`
    * gas `(amenity=gas)`
    * general `(amenity=general)`
    * gift `(amenity=gift)`
    * glaziery `(amenity=glaziery)`
    * greengrocer `(amenity=greengrocer)`
    * hairdresser `(amenity=hairdresser)`
    * hardware `(amenity=hardware)`
    * hearing\_aids `(amenity=hearing_aids)`
    * herbalist `(amenity=herbalist)`
    * hifi `(amenity=hifi)`
    * hunting `(amenity=hunting)`
    * interior\_decoration `(amenity=interior_decoration)`
    * jewelry `(amenity=jewelry)`
    * kiosk `(amenity=kiosk)`
    * kitchen `(amenity=kitchen)`
    * laundry `(amenity=laundry)`
    * massage `(amenity=massage)`
    * mobile\_phone `(amenity=mobile_phone)`
    * money\_lender `(amenity=money_lender)`
    * motorcycle `(amenity=motorcycle)`
    * musical\_instrument `(amenity=musical_instrument)`
    * newsagent `(amenity=newsagent)`
    * optician `(amenity=optician)`
    * organic `(amenity=organic)`
    * outdoor `(amenity=outdoor)`
    * paint `(amenity=paint)`
    * pawnbroker `(amenity=pawnbroker)`
    * pet `(amenity=pet)`
    * photo `(shop=photo)`
    * radiotechnics `(amenity=radiotechnics)`
    * seafood `(amenity=seafood)`
    * second\_hand `(amenity=second_hand)`
    * shoes `(amenity=shoes)`
    * sports `(amenity=sports)`
    * stationery `(amenity=stationery)`
    * tattoo `(amenity=tattoo)`
    * tobacco `(amenity=tobacco)`
    * toys `(amenity=toys)`
    * trade `(amenity=trade)`
    * vacuum\_cleaner `(amenity=vacuum_cleaner)`
    * video `(amenity=video)`
    * shop\_operator `(shop=*)`
    * other\_shop\_type `(shop=*)`
  * services `()`
    * drinking\_water `(amenity=drinking_water)`
    * post\_box `(amenity=post_box)`
    * post\_office `(amenity=post_office)`
    * recycling `(amenity=recycling)`
    * telephone `(amenity=telephone)`
    * toilet `(amenity=toilets)`
    * waste\_basket `(amenity=waste_basket)`
  * food `()`
    * cafe `(amenity=cafe)`
    * restaurant `(amenity=restaurant)`
    * cuisine `(cuisine=*)`
    * fast\_food `(amenity=fast_food)`
    * pub `(amenity=pub)`
    * bar `(amenity=bar)`
    * ice\_cream `(amenity=ice_cream)`
    * biergarten `(amenity=biergarten)`
    * food\_court `(amenity=food_court)`
    * vending\_machine `(amenity=vending_machine)`
  * leisure `()`
    * all\_in\_this\_category `(leisure=*)`
    * arts\_centre `(amenity=arts_centre)`
    * brothel `(amenity=brothel)`
    * cinema `(amenity=cinema)`
    * dance `(leisure=dance)`
    * dog\_park `(leisure=dog_park)`
    * fishing `(leisure=fishing)`
    * garden `(leisure=garden)`
    * golf\_course `(leisure=golf_course)`
    * ice\_rink `(leisure=ice_rink)`
    * marina `(leisure=marina)`
    * miniature\_golf `(leisure=miniature_golf)`
    * nature\_reserve `(leisure=nature_reserve)`
    * nightclub `(amenity=nightclub)`
    * park `(leisure=park)`
    * pitch `(leisure=pitch)`
    * playground `(leisure=playground)`
    * slipway `(leisure=slipway)`
    * social\_centre `(amenity=social_centre)`
    * sports\_centre `(leisure=sports_centre)`
    * stadium `(leisure=stadium)`
    * stripclub `(amenity=stripclub)`
    * swimming\_pool `(leisure=swimming_pool)`
    * theatre `(amenity=theatre)`
    * track `(leisure=track)`
    * water\_park `(leisure=water_park)`
  * tourism `()`
    * alpine\_hut `(amenity=alpine_hut)`
    * artwork `(amenity=artwork)`
    * attraction `(amenity=attraction)`
    * bed\_and\_breakfast `(amenity=bed_and_breakfast)`
    * camp\_site `(amenity=camp_site)`
    * caravan\_site `(amenity=caravan_site)`
    * chalet `(amenity=chalet)`
    * guest\_house `(amenity=guest_house)`
    * hostel `(amenity=hostel)`
    * hotel `(amenity=hotel)`
    * information `(amenity=information)`
    * motel `(amenity=motel)`
    * museum `(amenity=museum)`
    * picnic\_site `(amenity=picnic_site)`
    * theme\_park `(amenity=theme_park)`
    * viewpoint `(amenity=viewpoint)`
    * zoo `(amenity=zoo)`
  * other `()`
    * embassy `(amenity=embassy)`


The latest XML file can be seen [here](http://code.google.com/p/osmpoi-android/source/browse/assets/categories.xml)

_generated with [XSLT](CategoriesXSLT.md)_