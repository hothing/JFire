The classes in the package org.nightlabs.classloader are copied from
the project DelegatingClassLoader, because a nested JAR library is
not supported.

Neither does it work, if DelegatingClassLoader is itself an OSGI-bundle.
There seems to be no way to have the classes from that project being
separate - they must be included in this OSGI-bundle
(org.nightlabs.jfire.base.j2ee.osgi).

The reason why DelegatingClassLoader is separate is that this classloading
mechanism implemented there is completely indedependent of OSGI, while this
project requires OSGI (classes here implement OSGI-interfaces).

Marco.