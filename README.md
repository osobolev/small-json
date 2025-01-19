`small-json` is a small JSON parser/writer library.

Why another JSON parser?
It is inspired by `org.json` project and has similar API and performance. Differences are:

1. By default it adheres to JSON standard syntax
2. Different JSON syntax extensions can be enabled one by one (inspired by Jackson's `JsonReadFeature`) 
3. API is cleaner and simpler, only JSON parsing/writing is supported (no data binding)
4. It is really small (comparable to `json-simple` and `minimal-json`)

Compared to other small JSON libraries `json-simple` and `minimal-json`, `small-json` supports JSON syntax extensions and parsing customization.
