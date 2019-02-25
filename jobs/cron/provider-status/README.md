# Statuspage provider status cronjob

This cronjob runs every 30th minute, at full and half an hour (0/30).
It calculates the rate of aggregation instances where a provider is circuit broken.
The rate is then mapped to a `Statuspage` status;

| Rate           | Status                 |
|--------------: | ---------------------- |
| `0`            | `operational`          |
| `(0, 0.50]`    | `degraded_performance` |
| `(0.50, 0.75]` | `partial_outage`       |
| `(0.75, 1]`    | `major_outage`         |


## Creating a `Integrations - <market code>` component group

Prerequisites:
- Account on `statuspage.io`
- Access to `tink-backend-aggregation`
- Access to `buildkite`

0. Prerequisites fulfilled.
1. Go to `statuspage.io` and press `Components` in the menu to the left.
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
