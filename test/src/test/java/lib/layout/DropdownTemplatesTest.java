/*
 * The MIT License
 *
 * Copyright (c) 2026 Jenkins Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package lib.layout;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.UnprotectedRootAction;
import hudson.util.HttpResponses;
import java.util.List;
import jenkins.model.menu.event.ConfirmationEvent;
import jenkins.model.menu.event.Event;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.WebMethod;

@WithJenkins
class DropdownTemplatesTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void testConfirmationPostActionUrls() throws Exception {
        TestRootAction rootAction = j.jenkins.getExtensionList(UnprotectedRootAction.class).get(TestRootAction.class);
        assertNotNull(rootAction);

        JenkinsRule.WebClient wc = j.createWebClient().withThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = wc.goTo("dropdown-templates-test");

        String content = page.getWebResponse().getContentAsString();
        assertNotNull(content);

        DomNodeList<DomElement> overflowButtons = page.getElementsByTagName("button");
        DomElement overflowBtn = null;
        for (DomElement btn : overflowButtons) {
            if ("auto-overflow".equals(btn.getAttribute("data-type"))) {
                overflowBtn = btn;
                break;
            }
        }
        assertNotNull(overflowBtn, "Overflow button should exist");
    }

    @TestExtension
    public static final class TestRootAction extends Actionable implements UnprotectedRootAction {

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "Dropdown Templates Test";
        }

        @Override
        public String getUrlName() {
            return "dropdown-templates-test";
        }

        @Override
        public String getSearchUrl() {
            return getUrlName();
        }

        @Override
        public List<Action> getAppBarActions() {
            return List.of(
                    new ConfirmPostAction("Relative Action", "doPostAction?param1=val1&param2=val2"),
                    new ConfirmPostAction("Root Relative Action", "/dropdown-templates-test/doRootAction?param1=val1&param2=val2")
            );
        }

        @WebMethod(name = "doPostAction")
        public HttpResponse doPostAction(StaplerRequest2 req) {
            return HttpResponses.plainText("received:param1=" + req.getParameter("param1") + "&param2=" + req.getParameter("param2"));
        }

        @WebMethod(name = "doRootAction")
        public HttpResponse doRootAction(StaplerRequest2 req) {
            return HttpResponses.plainText("root:param1=" + req.getParameter("param1") + "&param2=" + req.getParameter("param2"));
        }
    }

    public static final class ConfirmPostAction implements Action {
        private final String name;
        private final String postTo;

        ConfirmPostAction(String name, String postTo) {
            this.name = name;
            this.postTo = postTo;
        }

        @Override
        public String getIconFileName() {
            return "symbol-trash";
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public String getUrlName() {
            return null;
        }

        @Override
        public Event getEvent() {
            return ConfirmationEvent.of("Confirm Action " + name, postTo);
        }
    }
}
