param(
    [Parameter(Mandatory = $true)]
    [string]$Version
)

$ErrorActionPreference = "Stop"
$tag = "v$Version"

git rev-parse --is-inside-work-tree | Out-Null

git diff --quiet
if ($LASTEXITCODE -ne 0) {
    throw "Working tree has unstaged changes."
}

git diff --cached --quiet
if ($LASTEXITCODE -ne 0) {
    throw "Index has staged but uncommitted changes."
}

git fetch --tags

git rev-parse $tag *> $null
if ($LASTEXITCODE -eq 0) {
    throw "Tag $tag already exists locally."
}

$remoteTag = git ls-remote --tags origin $tag
if ($remoteTag) {
    throw "Tag $tag already exists on origin."
}

git tag $tag
git push origin $tag

Write-Host "Release triggered for $tag"