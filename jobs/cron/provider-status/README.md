# Statuspage provider status cronjob

This cronjob runs every five minutes. It uses Prometheus to get a list of
providers with the rate of failed logins to successful ones for each. That ratio
is then used to update the corresponding component on https://tinksandbox.statuspage.io


| Rate           | Status                 |
|--------------: | ---------------------- |
| `0`            | `operational`          |
| `(0, 0.50]`    | `degraded_performance` |
| `(0.50, 0.75]` | `partial_outage`       |
| `(0.75, 1]`    | `major_outage`         |


## Creating a component group for a new market

The updater job will only update the statuspage components for the markets that
are added to the `GROUP_IDS` dict in `cronjob.py`. This means that whenever
there are integrations on a new market not already displayed on statuspage we
need to add a `component group` to statuspage and then configure that group in
`cronjob.py`. There are two ways of doing this: Manually or using a Python
script. 

### Using helper script 

The script is very basic, and requires the following steps to add one or more
new markets:

Prerequisites:
- Account on `statuspage.io`
- Access to `tink-backend-aggregation`
- Access to `buildkite`
- Ability to run a Python script depending on the `requests` library



0. Prerequisites fulfilled. 
1. Get your API key from `manage.statuspage.io` and export it as environment
   variable `STATUSPAGE_API_KEY` 
2. Update the `MARKETS` list in `create-markets.py` so it contains the markets
   you want to add. 
3. Run `create-markets.py` 
4. Copy the output from the script to `GROUP_IDS` dict in `cronjob.py`,
   replacing the current contents. The output contains all existing component
   groups.
5. Make a PR to `tink-backend-aggregation`, get it approved and merged.
6. Go to [Buildkite](https://buildkite.com/tink-ab/release-tink-backend-aggregation-statuspage-providers-cronjob)
   and release the latest version.


### Manually creating a `Integrations - <market code>` component group

Prerequisites:
- Account on `statuspage.io`
- Access to `tink-backend-aggregation`
- Access to `buildkite`

0. Prerequisites fulfilled.
1. Go to [manage.statuspage.io](https://manage.statuspage.io) and press `Components` in the menu to the left.
2. Press `+ ADD A COMPONENT`
3. When adding a new component do:
    - Set the component name to `Integrations - <market code>`.
    - In the dropdown `Component group`, choose `Create new component group`.
    - Set the component group name to the same as the component name, `Integrations - <market code>`.
    - (Optional) If you want you can also add a description.
    - Press `Save the component`
4. If not already there, go to `Components` in the menu to the left.
5. Choose to edit the newly created `Integrations - <market code>`.
6. In the url, copy the id of the component group.
    - https://manage.statuspage.io/pages/x1lbt12g0ryw/components/**j5rhg2mxl47y**/edit
7. In `tink-backend-aggregation/jobs/cron/provider-status/cronjob.py`
    - Add the id of the component group, together with the market code to the `GROUP_IDS` map.
8. Make a PR to `tink-backend-aggregation`, get it approved and merged.
9. Go to `https://buildkite.com/tink-ab/release-tink-backend-aggregation-statuspage-providers-cronjob` and release the latest version.
