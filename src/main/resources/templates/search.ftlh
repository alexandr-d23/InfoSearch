<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Search Results</title>
</head>
<body>
<#if search??>
    <h1>Search Results for: "${search}"</h1>
<#else>
    <h1>PutData to search urls: "${search}"</h1>
</#if>

<ul>
    <form action="${springMacroRequestContext.contextPath}/search" method="POST">
        <div class="data-form">
            Search: <input type="text" name="searchText"/>
            <br>
            <div class="data-button">
                <input type="submit" value="Find" name="color-button" class="btn btn-dark"/>
            </div>
        </div>
    </form>
    <#if results??>
        <#list results as result>
            <a href=${result.url}>${result.url}</a>
            cos = ${result.cos}
            <li></li>
        </#list>
    </#if>
</ul>
</body>
</html>