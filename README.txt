This project is a work-in-progress port, from Java to Groovy, of
the sample code of the book "Growing Object Oriented Software,
Guided By Tests" by Steve Freeman and Nat Pryce.

The port to Groovy was done by Paul King.

Current status:
  Wrote a gradle script to build everything
  Slight restructure to assist with using multiple technologies
  Wrote some FEST tests in Groovy which test to original java-app

The book's website:
  http://www.growing-object-oriented-software.com/

The original code:
  http://github.com/sf105/goos-code/

Use gradle to build, e.g.:
> gradle :java-app:build
> gradle :junit-windowlicker-tests:test
> gradle :junit-fest-tests:test
