[![Build Status](https://travis-ci.org/ucam-cl-dtg/picky.svg)](https://travis-ci.org/ucam-cl-dtg/picky)

# Picky

Picky facilitates efficient, reproducible and selective sharing of large scientific datasets.

# Documentation

  * [How to build](#how-to-build)
    * [Requirements](#requirements)
    * [Build Instructions](#build-instructions)
  * [How to use](#how-to-use)
    * [Indexing](#indexing)
        * [1. Prepare the dataset in a <em>source directory</em> suitable for sharing.](#1-prepare-the-dataset-in-a-source-directory-suitable-for-sharing)
        * [2. Index the source directory](#2-index-the-source-directory)
        * [3. Publish the dataset](#3-publish-the-dataset)
        * [4. Updating the dataset](#4-updating-the-dataset)
    * [Downloading](#downloading)
  * [Changelog](#changelog)
    * [0.1 (2015-03-27)](#01-2015-03-27)

# How to build

## Requirements
* Java 1.8+
* Maven 3.0+

## Build Instructions
1. checkout sources
2. run `mvn install` in project root

The build is going to produce two artefacts of interest:
- picky-indexer/target/picky-indexer-0.2-SNAPSHOT-jar-with-dependencies.jar
- picky-client/target/picky-client-0.2-SNAPSHOT.jar

# How to use

Picky consist of two parts, an *indexer* for server-side preparation and a *download client*.

## Indexing

To share a dataset using Picky, you need to perform the following steps:

### 1. Prepare the dataset in a *source directory* suitable for sharing.

Picky works on a directory as an entry point to your dataset. The directory may contain subdirectories and files. After downloading, Picky will reconstruct the *source directory* on the users machine, maintaining directory layout, file names and last modified timestamps. 

### 2. Index the source directory
    
To index the dataset, run `java -jar picky-indexer-0.2-SNAPSHOT-jar-with-dependencies.jar` with the following parameters:

Parameter | Req. | Description
--- |:---:|---
 `-s (--source)` | yes | Source directory to create index from (e.g. '/local/dataset')
`-t (--target)` | yes | Target directory to create index in (e.g. '/local/index')
`-r (--reference)` | yes | Unique userfriendly reference name
`-m (--tmp)` | yes | Tmp directory (e.g. '/tmp')
`-p (--parser)` | yes | Full-quallified class name of IEntryParser implementation
`-n (--description)` | no | Short dataset description
`-i (--icon)` | no | Dataset or provider icon file (e.g. '/local/icon.png')
`-u (--url)` | no | Website URL, e.g. further information related to the dataset
`-l (--logLevel)` | no | Log Level (Default: INFO)
`-c (--chunkSizeLimit)` | no | Chunk size limit in byte (Default: 5MB)
    
Depending on the size of the dataset and the available computational resources, indexing will take quite some time, as the entire dataset will be read, parsed, compressed, hashed and stored.
    
#### Resource considerations
    
*  **CPU** Indexing is usually CPU-bound and Picky makes use of all available cores, so increasing the number of cores will speed up indexing.
* **Memory** Picky tries to work with reasonable memory consumption. The minimum requirement depends solely on the nature of the dataset and the configuration. Since it Java, however, keep in mind that more memory reduces the need for garbage collection and thus speeds up indexing.
* **Storage** The target directory will need to have sufficient storage space to store the compressed (gzip) dataset. Indexing adds some additional information to the dataset, so expect the resulting index to be somewhat larger compared to compressing the bare dataset.
    
    
#### Pause, crash and continue
Indexing can take several days. You can kill the process at any time and restart it later. Should the process crash (e.g. for running out of memory), you can also start it again and it will continue its work. Should you required a clean start, delete the temp directory where interim result are cached. 

### 3. Publish the dataset
The index created by the previous step is literally a directory structure containing a lot of files used by the Picky download client. The index can be made available to others by a number of common file transfer techniques, including direct file access, sftp, and http.

A common way of publishing would hence be setting up an arbitrary webserver (e.g. nginx) to server the index directory. To access the dataset, clients will need to know the URL of the server as well as the reference name specified during indexing.

### 4. Updating the dataset
Updating the dataset is easy - simply repeat the indexing process, using the already existing index as target. Updating can be performed without interfering with clients downloading an already published version from the same index. If the same reference name is used, subsequent downloads will the point to the new version. You can, however, choose to incorporate some versioning scheme for the reference name, to allow access to prior versions of the dataset. Note that indexing will only add changed data to the index, so updates are fairly resource efficient both in terms of server storage and bandwidth. 

# Usage statistics upload
On begin and end of the download process, Picky tries to report usage statistics to the dataset publisher to allow for a better understanding of which parts of the dataset are relevant for clients. It will do so by posting a UTF-8 plain text file to the server URL + /analytics. The information collected are in detail:
- dataset Id
- dataset description
- number of chunks in dataset
- number of files in dataset
- average number of blocks in dataset
- file filter
- number of matching files
- selected entries
- number of files and directories to create, update or delete
- number of chunks to download
- remaining bytes to download
- bytes downloaded 

Note that no other information - especially no private information regarding the user's system or files - are transmitted. See [Analytics.java](https://github.com/ucam-cl-dtg/picky/blob/master/picky-client/src/main/java/uk/ac/cam/cl/dtg/picky/client/analytics/Analytics.java) for details.

## Downloading

To download a dataset, run `java -jar picky-client-0.2-SNAPSHOT.jar`. The client can be used as follows:

### Settings
Configure the server providing the index, the reference name, the target directory, a cache directory and a temp directory. Note that the download client will alter the target directory to synchronize it with the selected dataset subset. This includes *changing and deleting* any files present if necessary!

The cache directory stores compressed parts of the dataset and comes in handy if the dataset subset selection is changed later. It can, however, be safely deleted to reclaim some storage space if necessary.

![settings](https://raw.githubusercontent.com/ucam-cl-dtg/picky/master/documentation/images/client-settings.png)

### File selection
Subsets can be defined on file level by specifying a filter rule. The filter rule is a boolean JavaScript expression evaluated for each of the files present in the original dataset. Metadata privided during indexing are available for decision making, as well as file-independent functions such as `Math.random()`.

![file selection](https://raw.githubusercontent.com/ucam-cl-dtg/picky/master/documentation/images/client-file-selection.png)

### Entry selection
Subsets can also be defined on entry, i.e. subfile-level. Depending on the nature of the dataset, the user is presented a choice of key/value features available for entries within the dataset. The client will download and install only selected entries. 

![entry selection](https://raw.githubusercontent.com/ucam-cl-dtg/picky/master/documentation/images/client-entry-selection.png)


### Applying pending changes
Any time settings or selections are changed, the client analyses the target directory and calculates all changes required in order to make it match the specified dataset subset. Push 'Apply Changes' to actually start downloading data and applying the calculated changes to the specified target directory.

![apply changes](https://raw.githubusercontent.com/ucam-cl-dtg/picky/master/documentation/images/apply-changes.png)

# Changelog

## 0.2 (2015-04-21)
- Upload usage statistics
- Enhanced progress feedback
- Start/stop option

## 0.1 (2015-03-27)
- First version published