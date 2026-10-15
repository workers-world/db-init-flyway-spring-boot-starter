# Security Policy

## Supported versions

Security fixes are applied on the active development branches of this repository (currently the multi-module line on `dev_00_*` / `master`). Older single-module tags may not receive backports unless noted in a release.

## Reporting a vulnerability

**Please do not open a public GitHub issue for security-sensitive reports.**

Report vulnerabilities privately by emailing **[code@ongoingblog.org](mailto:code@ongoingblog.org)** with:

- A description of the issue and potential impact
- Steps to reproduce or a proof of concept (if available)
- Affected module(s), version or commit SHA, and any suggested fix

We aim to acknowledge reports within a few business days. We will coordinate disclosure timing with you when possible.

## HTTP admin and operational surfaces

This SDK may register HTTP endpoints (for example progress SSE, recycle-bin APIs, or optional demo static pages shipped with starters). **These endpoints are not authenticated by the SDK itself.** Host applications must:

- Place admin or operational routes behind application-level authentication and authorization
- Restrict exposure (network policy, reverse proxy, Spring Security, etc.)
- Avoid exposing debug or demo static assets in production unless explicitly intended

Treat any bundled HTML/JS helpers as integration samples unless your product documents them as first-class admin UI.
