package se.tink.backend.core;

import io.protostuff.Tag;

public class HtmlActivity {
    @Tag(1)
    protected String htmlElement;

	public String getHtmlElement() {
		return htmlElement;
	}

	public void setHtmlElement(String htmlElement) {
		this.htmlElement = htmlElement;
	}
}
