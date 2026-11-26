"""Render JDK 25 Javadoc type pages as a single GitHub Wiki page."""

import hashlib
from html import escape, unescape
from pathlib import Path
import posixpath
import re
import sys
from urllib.parse import unquote, urlsplit


def anchor(path, fragment=""):
    # Javadoc IDs can include signatures and punctuation. Use IDs safe in Wiki links.
    key = f"{path}#{fragment}".encode()
    return "api-" + hashlib.sha256(key).hexdigest()[:20]


def wiki_markup(path, body, types):
    # The Javadoc summary grid depends on CSS and JavaScript. Details carry the
    # declarations and full comments and render as ordinary HTML inside Markdown.
    description, separator, remainder = body.partition('<section class="summary">')
    if separator:
        _, details, members = remainder.partition('<section class="details">')
        body = description + details + members
    body = re.sub(r"<h1\b[^>]*>.*?</h1>", "", body, count=1, flags=re.S)

    def rewrite_link(match):
        url = urlsplit(unescape(match[1]))
        if url.scheme in ("http", "https", "mailto") or url.netloc:
            return match[0]
        target = path
        if url.path:
            target = posixpath.normpath(posixpath.join(posixpath.dirname(path), unquote(url.path)))
        if target in types:
            return f' href="#{anchor(target, unquote(url.fragment))}"'
        return ""

    def rewrite_tag(match):
        tag = match[0]
        tag = re.sub(r' href="([^"]*)"', rewrite_link, tag)
        tag, count = re.subn(r' id="([^"]*)"', "", tag)
        if count:
            raw_id = re.search(r' id="([^"]*)"', match[0])[1]
            tag = f'<a name="{anchor(path, unescape(raw_id))}"></a>{tag}'
        return tag

    return re.sub(r'<[a-zA-Z][\w-]*(?:[^">]|"[^"]*")*>', rewrite_tag, body).strip()


def generate(source, destination):
    types = {}
    for file in sorted(source.rglob("*.html")):
        doc = file.read_text(encoding="utf-8")
        main = re.search(r"<main\b[^>]*>(.*?)</main>", doc, re.S)
        if main and 'class="type-signature"' in main[1]:
            types[file.relative_to(source).as_posix()] = main[1]
    if not types:
        raise SystemExit(f"No Javadoc type pages found in {source}")

    page = ["<!-- Generated from Javadoc. -->", "Generated from the project's Java API."]
    index = []
    details = []
    for path, body in types.items():
        qualified_name = escape(path.removesuffix(".html").replace("/", "."))
        type_anchor = anchor(path)
        index.append(f"- [{qualified_name}](#{type_anchor})")
        content = wiki_markup(path, body, types)
        details.append(f'<a name="{type_anchor}"></a>\n\n## {qualified_name}\n\n<div>\n{content}\n</div>')

    page.extend(["\n".join(index), *details])
    destination.write_text("\n\n".join(page) + "\n", encoding="utf-8")


if __name__ == "__main__":
    generate(*map(Path, sys.argv[1:]))
