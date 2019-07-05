package io.jenkins.plugins.gitlabbranchsource.helpers;

import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.UriTemplateBuilder;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import java.io.IOException;
import java.net.URL;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class GitLabBrowser extends GitRepositoryBrowser {

    @DataBoundConstructor
    public GitLabBrowser(String projectUrl) {
        super(projectUrl);
    }

    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        return new URL(
                UriTemplate.buildFromTemplate(getRepoUrl())
                .literal("/commit")
                .path(UriTemplateBuilder.var("changeSet"))
                .build()
                .set("changeSet", changeSet.getId())
                .expand()
        );
    }

    @Override
    public URL getDiffLink(GitChangeSet.Path path) throws IOException {
        if (path.getEditType() != EditType.EDIT || path.getSrc() == null || path.getDst() == null
                || path.getChangeSet().getParentCommit() == null) {
            return null;
        }
        return diffLink(path);
    }


    @Override
    public URL getFileLink(GitChangeSet.Path path) throws IOException {
        if(path.getEditType().equals(EditType.DELETE)) {
            return diffLink(path);
        } else {
            return new URL(
                    UriTemplate.buildFromTemplate(getRepoUrl())
                            .literal("/blob")
                            .path(UriTemplateBuilder.var("changeSet"))
                            .path(UriTemplateBuilder.var("path", true))
                            .build()
                            .set("changeSet", path.getChangeSet().getId())
                            .set("path", StringUtils.split(path.getPath(), '/'))
                            .expand()
            );
        }
    }

    private URL diffLink(GitChangeSet.Path path) throws IOException {
        return new URL(
                UriTemplate.buildFromTemplate(getRepoUrl())
                        .literal("/commit")
                        .path(UriTemplateBuilder.var("changeSet"))
                        .fragment(UriTemplateBuilder.var("diff"))
                        .build()
                        .set("changeSet", path.getChangeSet().getId())
                        .set("diff", "#diff-" + getIndexOfPath(path))
                        .expand()
        );
    }

    @Extension
    public static class DescriptorImp extends Descriptor<RepositoryBrowser<?>> {
        public String getDisplayName() {
            return "GitLab";
        }

        @Override
        public GitLabBrowser newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            return req.bindJSON(GitLabBrowser.class, jsonObject);
        }
    }

}