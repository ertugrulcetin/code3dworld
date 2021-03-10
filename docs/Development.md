## Local Development Guide
To run frontend part of the app (Electron + Shadow CLJS), please follow the instructions:

`lein watch`

In another terminal session, run:

`electron .`

Now, it's time to start backend (3D Scene):

`cd backend-3d-scene`

`lein run`

---

## Building App for Prod

Make sure that you're at the root dir: `code3dworld`

`./sh/build.sh`

or

`./sh/build.sh zip` (Release purpose, creates a compressed app)
