# General Notes & Design
## Design
### Chosen Protocol & High Level Overview
The design of the project is to have a web service layer which will be responsible for downloading the needed file(s)
by chunks within HTTP server. HTTP protocol chosen as a wide accepted one.Although there exists a range download for
files in http however the author thinks that:
1. Range download design is more vulnerable for DDoS attacks since the client has control over the chunk. In current
   design the server defines the chunk size, so it close to impossible to block the server thread pool.
2. The author thinks the web service design (REST based) could be implemented by any language modern much more quickly
   today because of wide acceptance of the HTTP protocol high level tooling. It is much more complicated for people to
   think in protocol details (HTTP headers for range download) than in data transfer objects.
3. The control for the server side over the implementation within range downloads will be more complicated to code on
   the server side. It is lower details of the HTTP server.

### Technologies & Code Design
* The server will be based on Spring Boot technology because of the "deploy anywhere" requirement. As well, Spring Boot
already includes (almost) the all needed libraries as well the logger (slf4j) foundations. SLF4J will be not
reconfigured, as well its underlying concrete logger implementation, because if one will want to output to file she can
redirect the std output to file. By author thinking with such small applications it is redundant to have additional file
log (I could be wrong...)
* Once the Spring Boot uber jar will be executed it will check existence of two directories from the current run process
directory. These directories have names "config" and "out" with the appropriate meaning. If they do not exist they will
be created on demand. As well, there will checked existence of the configuration file (in "config"). If it does not
exist it will be created with default values. The user will be able to change the needed properties by her wish
* While the server running the server will check periodically existence of '*.log' files in outgoing directory. If there
are new files it will calculate their MD5 hash. This is because big files should be supported. Calculating hash of
multi-gigabyte files is a time consuming operation.
* There are two endpoints: /files and /chunk. Both of them only support GET HTTP operation and reply with JSON content
type.
    + The /files endpoint retrieves all '*.log' files' metadata. This is by design. Since user may want to retrieve
    some metadata again (later on this). Metadata consist of file name, creation time and file MD5 checksum. Checksum is
    needed by client to verify the content integrity of the file. (Thus, if the integrity fails because, for instance,
    the file was modified on server side during the transmitting the client should request the file checksum again.
    However, it will not happen on demand by server if the server running. Server will always reply with the firstly
    calculated checksum. This is by design too since once files are in outgoing directory they should not touched.
    If such event happens this should detected. The server should be restarted explicitly to recalculate checksums as
    per current design)
    + The /chunk endpoint requires the two additional parameters as chunk number and file name. The server will reply
    with the Iterator like structure. I.e. it will have an indicator whether continuation for the file exists. As well
    it will reply with content of given chunk size encoded by Base64 and MD5 chunk checksum before Base64 encoding.
    
## Development Process and Testing Strategy
The author has a strong opinion about "doing by need" approach. Nothing should be done because it just is required. The
Cargo Cult <https://en.wikipedia.org/wiki/Cargo_cult> today by author vision has a catastrophic volume. For instance,
writing unit testing for code coverage has no meaning. This is because the main goal of such process is the "code
coverage". Yes, the code will be covered but with useless testing. The testing is not a spherical cow in vacuum
<https://en.wikipedia.org/wiki/Spherical_cow> but it is an aspect of program (product) lifecycle. It should have a
meaning. It should test algorithmic correctness; test the critical part of the program that are about to change
(frequently); should describe some public or private APIs being a kind of documentation or some other meaningful
operations. The author believes that once the code if fully covered it at least has a twice complex support. Also,
the author believes that writing good testing is as complex as writing the modular code. Both of aspects are a kind of
art. So the unit test presented here are written when they were really needed or it is a fragile complex part.

The opposite of the unit test cult is the "self documented" code approach. For the most it is justification of the
laziness. Some part are complex because they are complex. No any self documentation will explain how, for instance,
quick sort works. Text is for humans, code is for machine. The code should be commented by need and public API should be
described. So whenever the author feels the need for comment it is there.

# Assumptions:

* It is assumed that connection between client and server is more or less reliable. Therefore HTTP is used. In real life
there should be TLS/SSL connection with signed certificate should be used.

* Application can support any file which ar of '*.log' pattern

* Files are sorted by creation time but modification. This is because the "younger" logs should be created closer to
current date but could be modified later. However, this is easy to change if needed.
