package ee.ttu.java.studenttester.core.enums;

public enum SourceSetType {

    /**
     * All source directories correspond to packages.
     */
    ROOT,

    /**
     * All packages are inside "src" directory.
     */
    SRC,

    /**
     * Source directories are in Gradle or Maven-like structure.
     */
    SRC_MAIN_JAVA

}
