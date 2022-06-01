package hexlet.code.controllers;

import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import hexlet.code.domain.query.QUrl;
import io.javalin.http.NotFoundResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class UrlController {

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        final int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("urls/index.html");
    };

    public static Handler createUrl = ctx -> {
        String urlToString = ctx.formParam("url");
        URL urlObject;

        try {
            urlObject = new URL(urlToString);
        } catch (MalformedURLException mue) {
            ctx.status(422);
            ctx.sessionAttribute("flash", "Incorrect URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        Url duplicateUrl = new QUrl()
                .name.equalTo(urlToString)
                .findOne();

        if (duplicateUrl != null) {
            ctx.status(422);
            ctx.sessionAttribute("flash", "The page already exists");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/urls");
            return;
        }

        String protocol = urlObject.getProtocol();
        String domain = urlObject.getAuthority();

        String urlToSave = protocol + "://" + domain;

        Url url = new Url(urlToSave);
        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
        System.out.println(ctx.fullUrl());
        System.out.println(ctx.url());
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .orderBy().id.desc()
                .findList();

        ctx.attribute("urlChecks", urlChecks);

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };
}
