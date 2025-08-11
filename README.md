# line-server

## How does your system work?
### Dependencies

To run, we need Java 21 and Maven installed.

For Java, I usually follow these instructions for SDKman because it is very simple to do: https://sdkman.io/install/ .

Once I have sdkman installed, one can do:
```bash
$ sdk install java 21.0.8-amzn
```

Note (optional if above fails): if that version is not available, one can do the following to check available ones:
```bash
$ sdk list java
```

To install maven, one can do:
```bash
$ sudo apt install maven
```
or even (with sdkman):
```bash
$ sdk install maven
```

### How to run
The system is a web app which exposes an endpoint (REST API) to get a line, given a line index, from a file which is passed in as an argument to `./run.sh` from the root of the project.

First we compile the project. Then, we run it, pointing to a file which must be placed at the root of the project.

Example:
```bash
$ ./build.sh
$ ./run.sh my_file_name_at_the_root_of_the_project
```

The following REST API is exposed:
```
GET /lines/<line index>
```

Helper to run:
```bash
$ curl localhost:8080/lines/1
```

### How it works (theory)
On startup, `LineService` is injected with the given `filepath` - from it, we open a `BufferedInputStream` to help us go
from line to line (in fact, byte by byte) and get a list of offsets (which equate to the starting byte, in memory, for each line). We do this by
storing a position each time we find '\n'.

With this information, we create some metadata chunks (each one holding N lines) which contain their own internal lists of offsets.
We do this because files can be very big and chunking helps with iterating smaller collections.

Then, when we request a given line index, we get its corresponding chunk and from its chunk-specific local offsets
we read the corresponding line.

There's also (for the reads on the exposed REST API) an in-memory cache.

## How will your system perform with a 1 GB file? a 10 GB file? a 100 GB file?

Because offsets are stored at the start, one of the columns holds the startup elapsed time.
A log is included on startup which should hold the value like so:
```
c.e.l.content.service.LineService : Time taken to read file: 22166.900 ms
```

| Size  | GET API | Startup Elapsed Time                                  |
|-------|---------|-------------------------------------------------------|
| 1GB   |         | Time taken to read file: 22166.900 ms                 |
| 10GB  |         | Not tested: would assume ≃ above * 10 ≃ 3.67 minutes  |            
| 100GB |         | Not tested: would assume ≃ above * 10 ≃ 36.67 minutes |         

## How will your system perform with 100 users? 10000 users? 1000000 users?
100 users should pose no problem.
For 10000 and 1000000, depending on reads per second values, we might want to start thinking about scaling this to be able to handle
more concurrent reads.
One thing I added because of this was an in-memory cache (for each instance), to hold reads of a given line value for a given line index.
We might have more popular lines, for instance, and this can speed things up for some scenarios.

## What documentation, websites, papers, etc did you consult in doing this assignment?

I brushed up on IO operations in Java mostly, and looked up strategies to read this kind of information in a more scalable way, i.e: instead of reading the file each time (obviously), we could preprocess - and also for more users, trying to use caching.

## What third-party libraries or other tools does the system use? How did you choose each library or framework you used?
Using spring boot framework for the wep app. Using this because it is quick to set up a project for this type of exercise.

For the in-memory cache, using Guava - it's fast to set up the dependency through maven, and it defaults to LRU which suits
our needs. Also, I have used it before, so ease of used plays a part here.

## How long did you spend on this exercise? If you had unlimited more time to spend on this, how would you spend it and how would you prioritize each item?
About 4-5 hours.

First, I would add tests. I did not add them, nor did I implement this with TDD - in a normal work setting, I would use TDD
and build things from the requirements themselves (documented as tests). This means unit tests, integration, and whichever else needed
for each individual ticket. I would prioritize this as a measure to keep quality with code.

Then, with tests, I would think about CI - getting a strategy to having a pipeline to run on each commit. For instance, we could make it run
unit tests and integration tests.

Second, I would focus on extending the cache to being a separate key-value store instance (ex: Redis), because of some reasons:
- So that the cache is not replicated per instance, as with huge files this will mean having it replicated per each client;
- Java has a limit on about 2GB on array elements - this can result in out of memories with larger user bases which access many different lines. I would prioritize this second since this would be a scaling issue and we might not be at that point yet in time.


## If you were to critique your code, what would you have to say about it?

The first thing that comes to mind is that I did not introduce tests (mentioned above) - I would definitely add test coverage.

Something else I can think about is I structured the directories by controller/service/model because this is a small exercise - for a real project
I would usually structure it by business domain (com.example.lineserver.content.line.controller/model/service) - with many domain entities being created it would
make it easier to find each one's code in the future.

I would probably add more logging (and add some lib to add metrics), and store them somewhere). Time reading the file is a good example.

Maybe add versioning to the REST API as well for future proofing breaking changes a bit better (did not add that here).
On the API still, document it and expose it to clients through swagger - maybe even starting a postman collection for future helping usage.

Finally, I would add some error handling around not being able to read the file, getting wrong inputs at different entrypoints, etc - try to make the code as resilient as possible.
