## Quick guide on how to boost categorization without transaction data

When going into new markets where we have little or no transaction data we must somehow achieve a decent categorization level in order to feel that we can release a Beta.

# Help, tips and tricks
1. If you want to extract data from Wikipedia lists, a copy paste will often give single letters on rows. Tip: do a find/replace with regular expression: "\n.{1}\n" --> "\n". Be gone single letters!

2. Yelp can be good for finding some stuff. Below is a super simple script I wrote for extracting the search results. It might of course become outdated later, but should be easy to modify. It goes through the first 100 search results. If there are fewer then than it's not a problem.

```
from bs4 import BeautifulSoup
import requests

url = "https://www.yelp.com/search?find_loc=Stockholm&cflt=painters"
url += "&start={}"

for i in range(0, 110, 10):
    page = requests.get(url.format(i))

    soup = BeautifulSoup(page.content, 'html.parser')

    names = soup.find_all("a", {"class": "biz-name"})

    for name in names:
        print(name.span.text)
```

# Step 1
Fetch OSM data. There's an OSM Python script and a bash script which uses the Python script to fetch the data. One bash script must be written for each categorization tree (since they have different category codes).

# Step 2
Fetch data with names of companies which provide known services such as Internet, electricity, video on demand etc. Unfortunately we haven't found some good API for this so we have to manually copy from wikipedia or other websites.

For some categories it makes sense to have global data and not only data per country (such as airlines) and then you can just copy paste what's in this document below.

**(1) Mobile network operators**
https://en.wikipedia.org/wiki/List_of_mobile_network_operators_of_Europe

**(2) Electricity suppliers**
https://en.wikipedia.org/wiki/Electricity_distribution_companies_by_country

**(3) Insurance companies**
https://en.wikipedia.org/wiki/Category:Insurance_companies_by_country

**(4) Video on demand**
https://en.wikipedia.org/wiki/Category:Video_on_demand_services

**(5) Internet**
https://en.wikipedia.org/wiki/Category:Internet_service_providers_by_country

**(6) Music streaming**
https://www.digitaltrends.com/music/best-music-streaming-services/

**(7) Cable TV**
https://en.wikipedia.org/wiki/List_of_cable_television_companies

**(8) Airlines**
https://en.wikipedia.org/wiki/World%27s_largest_airlines

**(9) Car rental**
https://en.wikipedia.org/wiki/Category:Car_rental_companies
https://www.which.co.uk/reviews/car-hire/article/car-hire-comparison/car-hire-broker-comparison

**(10) Cloud storage**
https://en.wikipedia.org/wiki/Category:Cloud_storage

**(11) Rent**
Rent is tricky. It might be so that there's a common pattern for rent payments in a country. For example, in Sweden it's common that the rent payment description is the name of a housing collective (Bostadsrättsförening). In this case, extracting all those names in the country could be a good idea. This is of course different in all countries and in some countries there might not be a good way to do this.

**(12) Home cleaning**
I found no Wiki lists on this one but Yelp worked. https://www.yelp.com/search?find_desc=Home+Cleaning+Service&find_loc=Stockholm. Use the Python script I wrote above!

**(13) Repairs**
Search on Yelp for plumbing, painters etc. and use my Python script above. However I found that electricians might be a bad category because that contained some electronics shopping places.


# Global data

**Video on demand**
3Player
9Now
56.com
ABC iview
Academic Earth
Acetrax
Acorn DVD
Afrinolly
AJ+
Akimbo
Alfa Omega
All 4
ALTBalaji
Amazon Instant Video UK
Amazon Video
Ameibo
User:Amitveblr/sandbox
Amplify (distributor)
Animax Germany
Anime Strike
AOL On
Apple TV
ArtBabble
Astro IPTV
BBC iPlayer
BBC Redux
BBC Store
Big Big Channel
BlackTV247
Blastro
Blastro Networks
Blim
Blip
Bloggingheads.tv
BoxTV.com
Brightcove
BritBox
BT TV
BTV247, Inc
C-SPAN Video Library
CBS All Access
CBS Innertube
CBSN
CCTV+
Censorship of the iTunes Store
CHT MOD
Cinemoz
Citytv.com.co
Comedy.com
Comparison of streaming media systems
Comparison of video hosting services
Comparison of video streaming aggregators
Computaris
Content delivery network
CONtv
Crackle (company)
CraveTV
Creativity (magazine)
Crunchyroll
CuriosityStream
Dailymotion
Daisuki (website)
DAZN
User:Delaw34/sandbox
DigitalCurriculum
DirecTV Cinema
DittoTV
Docurama
Dove Channel (streaming service)
DramaFever
East Asia TV
Eros Now
Essex TV
Eurocinema
EuroparlTV
ExerciseTV
Fabchannel.com
Facebook Watch
Fandango (company)
Feeln
Filemobile
Filipino On Demand
Film4oD
FilmDoo
FilmFlex
Filmklik
FilmOn
FilmStruck
Flix Premiere
FORA.tv
Foxtel Now
Foxtel On
FreeCast (company)
Funny or Die
Girlfriends Films
Global Wrestling Network
Go90
GoDigital
GoDigital Media Group
GoMedia
Gravitas Ventures
GreatAmericans.com
GuideDoc
HBO Go
HBO Now
HD share
HitBliss
Hoopla (digital media service)
HOOQ
HotMovies
Hotstar
Hulu
Hunter TV
I Want TV
Ibakatv
Icflix
Iflix
Imeem
In2TV
Infomaniak
Internet television
Inview Technology
IPTV
IQiyi
Irokotv
ITunes Store
ITV Hub
Ivi.ru
Jaman
JellyTelly
Kaltura
Kangaroo
KAONMEDIA
Kewego
Kinomap
KORTV
Le.com
Lightbox
List of Internet television providers
LiveLeak
Love Nature
LoveFilm
Mag Rack
MainStreaming
Mango TV
MaYoMo
Metacafe
MovieBeam
Movieclips
Movieland
Movielink
Microsoft Movies & TV
Movies Anywhere
MSN Soapbox
MSN Video Player
Mubi
MuchTV
Music Choice
Muziic
MUZU.TV
MX1 Ltd
My5
MyOutdoorTV.com
MyToons
MyTV
Nash TV
Netd.com
Netflix
New Japan Pro Wrestling World
The NewsMarket
NFB.ca
Night Flight
NinjaVideo
Now TV
NyooTV
On Demand
OnDemand
OneWorldTV
The Online Network
Openfilm
Ora TV
OVGuide
Oznoz
Pandora TV
PBS Distribution
Peer-to-Peer Assisted Streaming Solution
PictureBox Films
PlayStation Video
Playster
Pluto TV
Popcorn Time
Popcornflix

**(6)**
Spotify
Apple Music
Tidal
Pandora
SoundCloud

**(8)**
Ryanair
Egyptair
British Airways
Air France-KLM
Air China Cargo
International Airlines Group
United Airlines
Air France-KLM
Turkish Airlines
United Continental Holdings
Lufthansa Cargo
China Southern Airlines
Delta Air Lines
China Eastern Airlines
Emirates SkyCargo
Air China
Air Canada
Qatar Airways Cargo
UPS Airlines
Singapore Airlines Cargo
Lufthansa Group
All Nippon Airways
Korean Air Cargo
The Emirates Group
China Eastern
Qatar Airways
Southwest Airlines
American Airlines
Ryanair	 Ireland
easyJet
Cathay Pacific Cargo
FedEx Express
American Airlines Group
Emirates
Cargolux
China Eastern Airlines
Lufthansa

**(9)**
Argus Car Hire
Auto Europe
Do You Spain
Rentalcars
Zest Car Rental
ACE Rent a Car
Advantage Rent a Car
Alamo Rent a Car
Arnold Clark Vehicle Management
Auto Europe
Avis Budget Group
Avis Europe
Avis Rent a Car
Avis Southern Africa
BookCab
Breeze (company)
Budget Rent a Car
Bunk Campers
CanaDream
Car2Go
Carzonrent
Citer SA
Cruise America
Daimler Hire
Dollar Rent A Car
Dollar Thrifty Automotive Group
Drivezy
Enterprise Rent-A-Car
Europcar
Firefly (car rental)
First Car Rental
Flexicar (carsharing)
The Hertz Corporation
Holiday Autos
Irish Car Rentals
JustShareIt
Kemwel
LeasePlan UK
Lex Autolease
Localiza
Maven (car sharing)
Meru Cabs
MylesCar
National Car Rental
Payless Car Rental
Redspot Car Rentals
Rent-a-Wreck
Rīgas Satiksme
Shlomo Group
Sixt
Thrifty Car Rental
Tilden Rent-a-Car
Tourism Holdings Limited
Uhaul Car Share
Volercars
Wicked Campers
Zipcar
Zoomcar

**(10)**
Cloud storage
Amazon DynamoDB
Amazon Elastic Block Store
Amazon ElastiCache
Amazon Glacier
Amazon S3
Archival Disc
Axcient
Backblaze
Comparison of online backup services
Baidu Wangpan
Bitcasa
Block-level storage
BOSH (software)
Carbonite (online backup)
Cartika
Comparison of CDMI server implementations
Chomikuj.pl
Cirtas
Clearvision
Client-side encryption
Cloud Foundry
Cloud Data Management Interface
Cloud storage gateway
CloudBees
CloudByte
CloudMe
Cloudup
Comparison of file hosting services
Comparison of streaming media systems
Comparison of video hosting services
Content delivery network
Cooperative storage cloud
CTERA Networks
CtrlS
Cyberduck
Data center
DigitalOcean
Diino
Distributed file system for cloud
DocumentCloud
DriveHQ
Drop.io
Dropbox (service)
Egnyte
ElephantDrive
EMC Atmos
Emerald Program
Enstratius
EtherDrive
Evernote
FilesAnywhere
FlowVella
Fluidinfo
Free Haven Project
Gladinet
Gluster
GmailFS
Google Cloud Datastore
Google Drive
Google Storage
GreenButton
Heroku
Hibari (database)
HP Cloud
Humyo
I-drive
IASO Backup
ICloud
ICloud leaks of celebrity photos
Iland
Iron Mountain
Jelastic
Jungle Disk
KeepVault
Kubity
LIBOX
The Linkup
MainStreaming
Mega (service)
Memonic
Memopal
MiMedia
Minio
Mobile cloud storage
MobileMe
MObStor
Moozone
Mozy
Comparison of online music lockers
Nasuni
Nextcloud
Nirvanix
Norton Zone
Nutanix
Object storage
OneDrive
Open Compute Project
OpenAutonomy
OpenIO
OpenShift
Ovi (Nokia)
OwnCloud
Oystor
Panzura
Pdfvue
Peer-to-Peer Assisted Streaming Solution
Priority Matrix
Pydio
Rackspace Cloud
Recovery as a service
Remote backup service
Riak
RushTera
Scality
Scan-Optics
SecureSafe
SlideRocket
Software-defined storage
SpiderOak
StorSimple
Stratoscale
Streaming media
Sun Cloud
Swisscom
SwissDisk
Sync.com
Syncdocs
Syncplicity
Tahoe-LAFS
Tarsnap
Tresorit
Ubuntu One
Video spokesperson
WeTransfer
Windows Live Devices
Windows Live Mesh
Xeround
Yahoo! Briefcase
Zadara Storage
Zettabox
Zmanda Cloud Backup
ZumoDrive
