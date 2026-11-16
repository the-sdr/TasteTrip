# TasteTrip
## GitHub usage
Sam's GitHub account is on the free plan. Private repos share 500 MB of Actions storage and 2,000 Actions minutes a month. In Nov 2026 Restock's per-push APK artifacts (kept 90 days by default) reached 1.1 GB and maxed it out.
- Every `actions/upload-artifact` step sets `retention-days: 3` or less. Skip the upload when a release already carries the file.
- Ask Sam before adding a workflow that runs on every push. Prefer tags, `workflow_dispatch` or path filters.
- Delete artifacts once they're no longer needed: `gh api -X DELETE repos/the-sdr/<repo>/actions/artifacts/<id>`.
