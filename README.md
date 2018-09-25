# Timber2SLF4J

Convert a project from Timber to SLF4J

This little java program will search through all .java files in a specified folder (recursively), looking for certain Timber log statements. If found, the class will be updated to work with SLF4J.

## DETAILS

- for a given .java file do the following:

1)

- look for:

    import timber.log.Timber;

- if found, replace with the following:

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

2)

- look for a line with " class ... { " and add the following line below:

    private static final Logger log = LoggerFactory.getLogger(<filename>.class);


3)

- replace any "Timber.d(" lines with "log.debug" -- same with .i/.w/.e
