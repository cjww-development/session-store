[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/cjww-development/session-store.svg?branch=master)](https://travis-ci.org/cjww-development/session-store)
[ ![Download](https://api.bintray.com/packages/cjww-development/releases/session-store/images/download.svg) ](https://bintray.com/cjww-development/releases/session-store/_latestVersion)

session-store
=============

Session store caches data based on an identifier. Data is stored in key value pairs. Calls accessing a
session updates the sessions lastModified timestamp preventing the session from timing out (sessions time
out after an hour). 

How to run
==========

````````
sbt run
````````

This will start the application on port **8400**

Running tests
=============

```````````
sbt compile scalastyle coverage test it:test coverageReport
```````````

You can set an alias for this in **.bashrc** (ubuntu) or **.bash_profile** (mac)

Routes
======

|                    Path                  | Supported Methods |                       Description                         |
| -----------------------------------------| ------------------| --------------------------------------------------------- |
|  /session-store/session/:sessionId       |       POST        | Creates a session given the provided session id           |
|  /session-store/session/:sessionId/data  |       GET         | Fetches a particular session item given an id and key     |
|  /session-store/session/:sessionId       |       PUT         | Updates the session given a session id and encrypted body |
|  /session-store/session/:sessionId       |       DELETE      | Destroys the session when given a session id              |

Routes breakdown
================

#### POST /session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   | application-json | 
|   cjww-headers   | Z37kq_377_uCI3ex-Zr3HVZswYdwrIOdf8gV_YgdoHvMinul9BS16o6qoHqc6VPN8a4KLp78si_TnEEIn2kfSvEzo5vMvQtyh9fMFcTH15zD_RNpWs9Vg1Zbg27Hje_xm_RoyNJU6MnlRtMuSciFqA |

##### Responses
Created (201): The session has been created
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262",
    "method": "POST",
    "status": 201,
    "body": {
        "sessionId": "session-054bad30-b349-44db-8ce2-3fe55dbce262",
        "data": {},
        "modifiedDetails": {
            "created": {
                "$date": 1525015100294
            },
            "lastModified": {
                "$date": 1525015100320
            }
        }
    },
    "stats": {
        "requestCompletedAt": "2018-04-29T16:18:20.648"
    }
}
```

Bad request (400): A session matching the current session Id already exists
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262",
    "method": "POST",
    "status": 400,
    "errorMessage": "A session already exists against sessionId session-054bad30-b349-44db-8ce2-3fe55dbce262",
    "stats": {
        "requestCompletedAt": "2018-04-29T19:37:09.958"
    }
}
```

Internal server error (500): An unknown problem prevented the session from being created; check the application logs
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262",
    "method": "POST",
    "status": 500,
    "errorMessage": "There was a problem caching the session data for session session-054bad30-b349-44db-8ce2-3fe55dbce262",
    "stats": {
        "requestCompletedAt": "2018-04-29T20:04:30.598"
    }
}
```  

#### GET /session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262/data

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   |    text-plain    | 
|   cjww-headers   | Z37kq_377_uCI3ex-Zr3HVZswYdwrIOdf8gV_YgdoHvMinul9BS16o6qoHqc6VPN8a4KLp78si_TnEEIn2kfSvEzo5vMvQtyh9fMFcTH15zD_RNpWs9Vg1Zbg27Hje_xm_RoyNJU6MnlRtMuSciFqA |

##### Responses
Ok (200): A session has been found matching the session Id
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262/data",
    "method": "GET",
    "status": 200,
    "body": {
        "sessionId": "session-054bad30-b349-44db-8ce2-3fe55dbce262",
        "data": {
            "testKey": "testValue"
        },
        "modifiedDetails": {
            "created": {
                "$date": 1525030508572
            },
            "lastModified": {
                "$date": 1525030771897
            }
        }
    },
    "stats": {
        "requestCompletedAt": "2018-04-29T20:39:36.560"
    }
}
```

Forbidden (403): No matching session could be found
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce26/data",
    "method": "GET",
    "status": 403,
    "errorMessage": "[validateSession] - Session doesn't exist, action forbidden",
    "stats": {
        "requestCompletedAt": "2018-04-29T20:36:36.108"
    }
}
```

#### GET /session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262/data?key=testValue

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   |    text-plain    | 
|   cjww-headers   | Z37kq_377_uCI3ex-Zr3HVZswYdwrIOdf8gV_YgdoHvMinul9BS16o6qoHqc6VPN8a4KLp78si_TnEEIn2kfSvEzo5vMvQtyh9fMFcTH15zD_RNpWs9Vg1Zbg27Hje_xm_RoyNJU6MnlRtMuSciFqA |

##### Responses
Ok (200): A session has been found and a data item has been found matching the key
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262/data",
    "method": "GET",
    "status": 200,
    "body": "testValue",
    "stats": {
        "requestCompletedAt": "2018-04-29T20:39:36.560"
    }
}
```

No content (204): A session has been found but there was no data matching the key

#### PATCH /session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   | application/json | 
|   cjww-headers   | Z37kq_377_uCI3ex-Zr3HVZswYdwrIOdf8gV_YgdoHvMinul9BS16o6qoHqc6VPN8a4KLp78si_TnEEIn2kfSvEzo5vMvQtyh9fMFcTH15zD_RNpWs9Vg1Zbg27Hje_xm_RoyNJU6MnlRtMuSciFqA |

##### Request body
```json
//Can specify n key value pairs
{
   "testKey" : "testValue"
}
```

##### Responses
Ok (200): All key values were patched into the session
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262",
    "method": "PATCH",
    "status": 200,
    "body": {
        "testKey1": "Updated",
        "testKey2": "Updated",
        "testKey3": "Updated"
    },
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```

Internal server error (500): Some or all key value pairs weren't patched into the session
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262",
    "method": "PATCH",
    "status": 200,
    "errorMessage": {
        "testKey1": "Updated",
        "testKey2": "Updated",
        "testKey3": "Problem updating"
    },
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
``` 

#### DELETE /session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   | application/json | 
|   cjww-headers   | Z37kq_377_uCI3ex-Zr3HVZswYdwrIOdf8gV_YgdoHvMinul9BS16o6qoHqc6VPN8a4KLp78si_TnEEIn2kfSvEzo5vMvQtyh9fMFcTH15zD_RNpWs9Vg1Zbg27Hje_xm_RoyNJU6MnlRtMuSciFqA |

##### Responses
No content (204): The session has been deleted

Internal server error (500): There was a problem deleting the session
```json
{
    "uri": "/session-store/session/session-054bad30-b349-44db-8ce2-3fe55dbce262",
    "method": "DELETE",
    "status": 500,
    "errorMessage": "There was a problem deleting the session",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
``` 

License
=======
This code is open sourced licensed under the Apache 2.0 License
