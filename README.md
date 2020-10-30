# Evolutionary grammer-based fuzzing for JSON RCP APIs

 - - - -

*Research purpose: create a tool that generates tests using (evolutionary) grammer-based fuzzing for a JSON RCP API (system-level).*

Potential research questions (will be adjusted as the research progresses):
1. How effective is evolutionary grammer-based fuzzing to achieve test coverage on a JSON RCP API? (possible subquestion: try out different EAs)
2. What test coverage can be obtained with the created tool?
3. How efficient is the created tool? / How does the tool compare to existing techniques that measure test coverage?
4. Which kind of attacks (that target certain vulnerabilities) can be most effectively generated?

 - - - -

## Steps to take (for now)
1. Make connection to APIs (generic interface to communicate with APIs)
2. Create a grammar that results into valid requests
3. Build a grammer-based fuzzer
4. Create an evolutionary algorithm to create mutations in the grammar

 - - - -

## Issues to think about
* How do various JSON RCP APIs compare to each other (are they alike)?
* How to define fault detection / how to define the fitness function?