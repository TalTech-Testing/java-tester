```
Usage: StudentTester [options]
  Options:
  * --code, -c
      Path where the testable code is located
  * --tests, -t
      Path where the matching unit tests are located
  * --runners, -r
      Comma-separated list of actions to run (see below)
    --plainTextOutputInJson, -jsontxt
      Include the plain text report in JSON (overrides --plainTextOutput).
      Default: false
    --plainTextOutput, -txt
      Instead of JSON, print a plain text report
      Default: false
    --out, -o
      Output file name, if not defined, then stdout will be used
    --checkstyleXml, -csxml
      Checkstyle XML rule file. You can also include checkstyle.xml in the 
      tests folder
      Default: /sun_checks.xml
    --separateCompilation, -csep
      Compile each unit test separately to skip ones that can't be compiled
      Default: true
    --temp, -tmp
      Path used for temporary files, using system-provided location by default
    --deleteTmp, -del
      Delete temporary folder before exit
      Default: false
    --timeout, -to
      Maximum number of milliseconds each unit test can run before being 
      terminated 
      Default: 15000
    -O
      Additional options to pass to the compiler. By default the source path 
      and UTF-8 encoding are forced
      Syntax: -Okey=value
    -U
      Parameters to pass to unit tests. Refer to TestNG's @Parameters 
      documentation 
      Syntax: -Ukey=value

Runners can be any of the following: CHECKSTYLE,REPORT,COMPILER,TESTNG
```