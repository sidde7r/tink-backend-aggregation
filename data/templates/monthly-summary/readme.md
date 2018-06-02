### Convert svg to png
`brew install ImageMagick`

`mogrify -format png -background none *.svg`

Resize with (for every image)

`convert budget_0.png -resize 70x70\! budget_0.png`

`convert budget_search_0.png -resize 70x70\! budget_search_0.png`

### Inline html
http://templates.mailchimp.com/resources/inline-css/

#### Notes
* it changes List<ClassName> to List<classname> and adds weird spaces so these needs to be changed
* it adds end tags for above in the end of the document that needs to be removed
