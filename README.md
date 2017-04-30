[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/cjww-development/session-store.svg?branch=master)](https://travis-ci.org/cjww-development/session-store)
[ ![Download](https://api.bintray.com/packages/cjww-development/releases/session-store/images/download.svg) ](https://bintray.com/cjww-development/releases/session-store/_latestVersion)

session-store
=============

Scala Play! backend service for storing temporary user data based on session id

How to run
==========

```````````````
sbt run
```````````````

This will start the application on port **8400**

How to run tests
================

```````````
sbt compile scalastyle coverage test it:test coverageReport
```````````

You can set an alias for this in **.bashrc** (ubuntu) or **.bash_profile** (mac)

Routes
======

| Path                                                  | Supported Methods |                       Description                         |
| ----------------------------------------------------- | ------------------| --------------------------------------------------------- |
|       /session-store/session/:sessionId/cache         |       POST        | Creates a session given the provided session id           |
|       /session-store/session/:sessionId/data/:key     |       GET         | Fetches a particular session item given an id and key     |
|       /session-store/session/:sessionId               |       PUT         | Updates the session given a session id and encrypted body |
|       /session-store/session/:sessionId/destroy       |       DELETE      | Destroys the session when given a session id              |