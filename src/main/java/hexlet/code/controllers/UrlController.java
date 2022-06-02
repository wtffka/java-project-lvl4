package hexlet.code.controllers;

import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import hexlet.code.domain.query.QUrl;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
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
            ctx.render("index.html");
            return;
        }

        Url duplicateUrl = new QUrl()
                .name.equalTo(urlToString)
                .findOne();

        if (duplicateUrl != null) {
            ctx.status(422);
            ctx.sessionAttribute("flash", "The page already exists");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }

        String protocol = urlObject.getProtocol();
        String domain = urlObject.getAuthority();

        String urlToSave = protocol + "://" + domain;

        Url url = new Url(urlToSave);
        url.save();

        ctx.sessionAttribute("flash", "The page added successfully");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
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


    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        HttpResponse<String> response;
        try {
            response = Unirest.get(url.getName()).asString();

            int responseCode = response.getStatus();
            Document body = Jsoup.parse(response.getBody());

            String title = body.title();
            String description = null;
            if (body.selectFirst("meta[name=description]") != null) {
            description = body.selectFirst("meta[name=description]").attr("content");
            }

            String h1 = null;

            if (body.selectFirst("h1") != null) {
                h1 = body.selectFirst("h1").text();
            }

            UrlCheck check = new UrlCheck(responseCode, title, h1, description, url);
            check.save();

            ctx.sessionAttribute("flash", "The page checked successfully");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException ue) {
            ctx.sessionAttribute("flash", "The page is unavailable or doesn't exist");
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + id);
};
}
