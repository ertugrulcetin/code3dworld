## Local Development Guide
To run frontend part of the app (Electron + Shadow CLJS), please follow the instructions:

`lein watch`

In another terminal session, run:

`electron .`

Now, it's time to start backend (3D Scene):

`cd backend-3d-scene`

`lein run`

---
 
## Building 3D Scene for Prod

`cd backend-3d-scene`


`./build.sh`

**Important:** Make sure that **JPACKAGE_PATH** has Oracle JDK 14 path in `build.sh`

---

## Building App for Prod

Make sure that you're at the root dir: `code3dworld`

`./build.sh`

or

`./build.sh zip` (Release purpose, creates a compressed app)
