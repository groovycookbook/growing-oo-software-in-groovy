This project is a work-in-progress port from Java to Groovy of
the sample code of the book "Growing Object Oriented Software,
Guided By Tests" by Steve Freeman and Nat Pryce. The intention
is not to replace it with an exact port of the Java code in
Groovy but to highlight some of the Groovy technologies available.

The port to Groovy was done by Paul King.

Current status:
  Wrote gradle scripts to build everything
  Slight restructure to assist with using multiple technologies
  Wrote some FEST tests in Groovy which test the original Java app

The original book's website:
  http://www.growing-object-oriented-software.com/

The original code:
  http://github.com/sf105/goos-code/

Use gradle to build, e.g.:
> gradle :java-app:build
> gradle :junit-windowlicker-tests:test
> gradle :junit-fest-tests:test
