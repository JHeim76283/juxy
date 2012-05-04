Juxy
====

Juxy is a library for unit testing XSLT stylesheets from Java using JUnit.

This is a fork of [juxy.tigris.org](http://juxy.tigris.org).

[Quick introduction](http://juxy.tigris.org)

Changes in this fork:

* Converted the build system from Ant to Gradle.
* Added samples written in Groovy.
* New class JuxyTextSupport for JUnit4 tests.

Sample
------
The following example is a unit test written in Groovy. It uses the DOMBuilder to generate the input document inline in the test.

	import groovy.xml.DOMBuilder
	import org.junit.Test
	import org.tigris.juxy.JuxyTestSupport;

	public class TransformationTest extends JuxyTestSupport {
		@Test
		public void twoAuthors() {
			setStylesheet("src/test/resources/transform.xsl")
			
			setDocument(DOMBuilder.newInstance().books() {
				book() {
					authors() {
						author("Andrews, Bob")
						author("Cooper, Alice")
					} 
				}
			})

			def result = applyTemplates()

			xpathAssert("//author[1]/firstName", "Bob").eval(result)
			xpathAssert("//author[2]/firstName", "Alice").eval(result)
		}
	}

Building
--------

You don't need to have Gradle installed to build Juxy. The wrapper script "gradlew" will download Gradle automatically.

Build the sources:
    
	gradlew build

Install the juxy jar into your local Maven repository:
    
	gradlew install

To deploy the juxy jar to a remote Maven repository, set the properties 'uploadRepositoryUrl', 'uploadSnapshotRepositoryUrl', 'uploadRepositoryUsername', and 'uploadRepositoryPassword' in ~/.gradle/gradle.properties and call

	gradlew uploadArchives

Generate Eclipse project files:

	gradlew eclipse
