package org.littlegit.core.util

import java.util.*

/**
 * helper class to check the operating system this Java VM runs in
 *
 * please keep the notes below as a pseudo-license
 *
 * http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
 * compare to http://svn.terracotta.org/svn/tc/dso/tags/2.6.4/code/base/common/src/com/tc/util/runtime/Os.java
 * http://www.docjar.com/html/api/org/apache/commons/lang/SystemUtils.java.html
 */
actual object OperatingSystemUtils {

    // cached result of OS detection
    private var _detectedOS: OSType? = null
    /**
     * detect the operating system from the os.name System property and cache
     * the result
     *
     * @returns - the operating system detected
     */
    actual val osType: OSType
        get() {

            val operatingSystem = java.lang.System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)

            val osType = if (operatingSystem.indexOf("mac") >= 0 || operatingSystem.indexOf("darwin") >= 0) {
                OSType.MacOS
            } else if (operatingSystem.indexOf("win") >= 0) {
                OSType.Windows
            } else if (operatingSystem.indexOf("nux") >= 0) {
                OSType.Linux
            } else {
                OSType.Other
            }

            _detectedOS = osType
            return osType
        }
}