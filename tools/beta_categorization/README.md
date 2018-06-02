# Beta Categorization
For general tips and tricks, refer to [EXTRACTING_COMPANY_DATA](EXTRACTING_COMPANY_DATA.md) written by @ninaolo. The Yelp script from that section has been updated and usage is described below.

# Python modules
Easiest way to install the required modules is to use [virtualenv](https://virtualenvwrapper.readthedocs.io/en/latest/) and then inside your newly created environment run the following command: `pip install -r requirements.txt`

# Yelp data (deprecated)

To access Yelp API you'll need to have an access token exposed in `YELP_API_TOKEN` environment variable. To get the token you'll need to create a Yelp application here: https://www.yelp.com/developers/v3/manage_app (a Yelp account id required). In addition to that you'll need to enable Developer Beta to get access to GrapQL API.
For extracting data from Yelp's API, use [`get_yelp_data_by_area_graphql.sh`](get_yelp_data_by_area_graphql.sh) and supply a two-letter country code in [ISO-3166-1](https://www.iso.org/glossary-for-iso-3166.html) format.
If you are interested in the exact category mappings or would like to make any changes feel free to first check out the available categories from Yelp [here](https://www.yelp.se/developers/documentation/v3/category_list).

_Use of Yelp data for categorization models is deprecated as it introduces noise and ends up having a negative effect. See [the Trello card](https://trello.com/c/TmfBEvzt/303-evaluate-if-removing-yelp-data-improves-categorization) for details._

# OSM data
Data from OpenStreetMap can be fetched using [get_osm_data_by_area.sh](get_osm_data_by_area.sh) by supplying it a two-letter country code in [ISO-3166-1](https://www.iso.org/glossary-for-iso-3166.html) format.

# Combiner
In the `global_data`-folder there are mapped categories for common international companies, mostly airlines and online service providers. The `combiner.py` script provides an easy way to combine the different files into one aggregated list of category -> company-mappings.
### Example usage:
```
python3 combiner.py --country_folder denmark --out denmark_combined.txt
```

# Launching a new market
Before launching a new market we need to provide some initial data so that we can train the fasttext model for that specific market. First determine if you would like to use only data from _Yelp_, _OpenStreetMap_, or both.
1. Use the provided scripts to fetch the desired data
2. Put them in a folder named after your preferences (though preferably the name of the country, e.g. `finland`)
3. Use the `combiner.py`-script to combine the fetched data with the global data
4. Upload the combined file
4. Done!

# fastText version

Right now fastText binary for production is built on an ad hoc basis manually.
As fastText models are binary incompatible between versions the same commit
needs to be used to build the binary to build the models. Currently it's
facebookresearch/fastText@f24a781021862f0e475a5fb9c55b7c1cec3b6e2e
