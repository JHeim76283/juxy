
import groovy.xml.DOMBuilder

import org.junit.Before;
import org.junit.Test

import org.tigris.juxy.JuxyTestSupport;


public class TransformationTest extends JuxyTestSupport {
	@Before
	public void setUp() {
		setStylesheet("src/test/resources/transform.xsl")
	}

	@Test
	public void singleAuthor() {
		setStylesheet("src/test/resources/transform.xsl")
		
		setDocument(DOMBuilder.newInstance().books() {
			book() {
				authors() {
					author("Andrews, Bob")
				} 
			}
		})

		def result = applyTemplates(xpath("//author"))

		xpathAssert("author/firstName", "Bob").eval(result)
		xpathAssert("author/lastName", "Andrews").eval(result)
	}

	@Test
	public void twoAuthors() {
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

	@Test
	public void authorIsEmpty() {
		setDocument(DOMBuilder.newInstance().books() {
			book() {
				authors() {
					author("")
				} 
			}
		})

		def result = applyTemplates(xpath("//author"))

		xpathAssert("author/firstName", "").eval(result)
		xpathAssert("author/lastName", "").eval(result)
	}

	@Test
	public void tolower() {
		setDocument(DOMBuilder.newInstance().title("My BOOK"))

		def result = callTemplate("tolower")

		xpathAssert(".", "my book").eval(result)
	}
}
