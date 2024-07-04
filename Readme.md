# PROV-IDEA Provenance Inspector

This tool has been developed as part of the PROV-IDEA [1] approach to exploit provenance data using SPARQL [2]. 

PROV-IDEA is presented as a PROV-Interoperable Database Evolution Approach aimed at integrating provenance information into relational database schema evolution, using PROV as provenance framework. PROV-IDEA proposes the use of *PROV templates* [3], expressed in PROV [4], for Schema Modification Operators (SMOs) and Data Modification Operators (DMOs) to structure the 
provenance to be generated from database evolution. More specifically, the approach establishes a set of PROV Templates to represent SMOs and DMOs. These templates contain
variables that act as placeholders for values. On the other hand, following the approach, as SMOs and DMOs take place, *sets of bindings* are generated, which are mainly 
variable-value pairs that associate variables of the templates and values. Finally, the *expansion algorithm* [3] can be used to generate *PROV documents* from templates, 
by replacing all variables with values found in a set of bindings, obtaining the corresponding PROV document ready for provenance consumption.

*PROV-IDEA Provenance Inspector* has been developed to exploit such PROV documents. More specifically, this tool allows the interested user to query provenance data in TURTLE format
using the different SPARQL query forms, that is, SELECT, DESCRIBE, CONSTRUCT, and ASK [2]. In particular, the tool is expected to be used with the PROV-IDEA Expander tool [5] developed 
to generate PROV documents from bindings with schema evolution provenance data, as established by the PROV-IDEA approach [1]. 

The tool provides three panels. The top panel includes several radio buttons (one per each SPARQL query form), together with a ‘Run’ button to execute the desired query.
The query would have to be typed in the left panel, so that, once executed the query, the right panel would display the result, either in table format (SELECT queries) or in PROV graphic format (DESCRIBE, CONSTRUCT, and ASK).

## Project's main content

The repository mainly contains the following folders: 
- **src/main/resources/data**: with a set of PROV documents which includes the schema evolution provenance data in TURTLE format obtained for a concrete *running example*. Such documents have been generated by using the *PROV-IDEA Expander* tool [5].
- **src/main/resources/queries**: with a set of SPARQL queries defined to exploit such PROV documents. The documentation of the concrete queries, together with the *running example*, is available in the Supplementary Material of PROV-IDEA [1].
- **src/main/java/QuestionsManager**: it includes the interface and utility classes, being  **TextEditor.java** the main class.
- **src/main/java/results**: its main purpose is to store the files generated from the execution of the queries (**resultSet.ttl**, **resultSet.txt**, **resultSet.png** and **resultSet.svg**) and the two files used as answers for ASK questions (**true.ttl** and **false.ttl**).

## Setup environment

For testing *PROV-IDEA Provenance Inspector* you'll have to use JDK-1.8.
      
## Running

To test *PROV-IDEA Provenance Inspector* you'll have to run the **TextEditor**.

## References

[1] PROV-IDEA. Supplementary material. Available at: https://zenodo.org/records/10701239.
[2] SPARQL 1.1 Query Language. Available at [https://www.w3.org/TR/sparql11-query/](https://www.w3.org/TR/sparql11-query/).
[3] Luc Moreau, Belfrit Victor Batlajery, Trung Dong Huynh, Danius Michaelides, and Heather Packer. 2018. A Templating System to Generate
Provenance. IEEE Transactions on Software Engineering 44, 2 (2018), 103–121.
[4] Paul Groth and Luc Moreau (eds.). 2013. PROV-Overview. An Overview of the PROV Family of Documents. W3C Working Group Note NOTE-prov-
overview-20130430. World Wide Web Consortium. Available at [www.w3.org/TR/2013/NOTE-prov-overview-20130430/](www.w3.org/TR/2013/NOTE-prov-overview-20130430/).
[5] PROV-IDEA Expander. Available at [https://www.w3.org/TR/sparql11-query/](https://github.com/PROV-IDEA/PROV-IDEA-Expander/).


