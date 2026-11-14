param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path,
    [switch]$SkipHashes
)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath($ProjectRoot)
$skillRoot = Join-Path $project '.agents/skills'
$issues = [Collections.Generic.List[string]]::new()
$names = [Collections.Generic.HashSet[string]]::new()
$required = @('java-game-development','2d-platformer-development','kanji-handwriting-recognition',
    'kanji-stroke-order-validation','inkslayer-spellcasting','elemental-combat-system',
    'enemy-ai-development','boss-battle-design','metroidvania-world-design','japanese-learning-design',
    'pixel-art-pipeline','game-dialogue-system','game-save-system','java-game-testing','inkslayer-project-context')
$folders = @(Get-ChildItem -LiteralPath $skillRoot -Directory)
foreach ($folder in $folders) {
    $entry = Join-Path $folder.FullName 'SKILL.md'
    if (-not (Test-Path -LiteralPath $entry -PathType Leaf)) {
        $issues.Add("Missing SKILL.md: $($folder.Name)")
        continue
    }
    $body = [IO.File]::ReadAllText($entry).Replace("`r`n", "`n")
    $front = [regex]::Match($body, '\A---\n(?<yaml>.*?)\n---(?:\n|$)', 'Singleline')
    if (-not $front.Success) { $issues.Add("Invalid frontmatter: $entry"); continue }
    $fields = @{}
    foreach ($line in $front.Groups['yaml'].Value.Split("`n")) {
        # Intentionally accepts only the two unquoted single-line string fields used by this pack.
        $field = [regex]::Match($line, '^(name|description): ([A-Za-z0-9][^\r\n]*)$')
        if (-not $field.Success) { $issues.Add("Unsupported frontmatter line in $entry : $line"); continue }
        $key = $field.Groups[1].Value
        if ($fields.ContainsKey($key)) { $issues.Add("Duplicate key in $entry : $key") }
        $fields[$key] = $field.Groups[2].Value
    }
    $name = [string]$fields['name']
    $description = [string]$fields['description']
    if ($name -notmatch '^[a-z0-9]+(-[a-z0-9]+)*$' -or $name.Length -gt 64 -or $name -ne $folder.Name) {
        $issues.Add("Invalid folder/name agreement: $entry")
    }
    if (-not $names.Add($name)) { $issues.Add("Duplicate skill name: $name") }
    if ([string]::IsNullOrWhiteSpace($description) -or $description.Length -gt 1024 -or
        $description -match '[<>]|: | #|\s$') { $issues.Add("Invalid scalar description: $entry") }
    foreach ($section in @('Workflow','anti-patterns','Definition of Done','Validation')) {
        if ($body -notmatch [regex]::Escape($section)) { $issues.Add("Missing $section in $entry") }
    }
    if ($body -match '\[TODO:|\[INSERT|PLACEHOLDER_DESCRIPTION') { $issues.Add("Unfinished scaffold: $entry") }
}
foreach ($name in $required) {
    if (-not $names.Contains($name)) { $issues.Add("Missing requested skill: $name") }
}
$documents = @((Join-Path $project 'AGENTS.md')) +
    @(Get-ChildItem -LiteralPath (Join-Path $project 'docs') -Filter '*.md' -File | ForEach-Object FullName) +
    @(Get-ChildItem -LiteralPath $skillRoot -Filter '*.md' -Recurse -File | ForEach-Object FullName)
foreach ($file in $documents) {
    $body = [IO.File]::ReadAllText($file)
    foreach ($link in [regex]::Matches($body, '\]\(([^)]+)\)')) {
        $target = $link.Groups[1].Value
        if ($target -match '^[a-zA-Z]+:|^#') { continue }
        $target = ($target -split '#')[0]
        if (-not (Test-Path -LiteralPath (Join-Path (Split-Path $file) $target))) {
            $issues.Add("Broken local link in $file : $target")
        }
    }
}
$manifest = Join-Path $project 'docs/skills-lock.json'
if (-not $SkipHashes -and (Test-Path -LiteralPath $manifest)) {
    $lock = Get-Content -LiteralPath $manifest -Raw | ConvertFrom-Json
    $lockedPaths = @($lock.files | ForEach-Object path)
    $actualPaths = @(Get-ChildItem -LiteralPath $skillRoot -Recurse -File | ForEach-Object {
        $_.FullName.Substring($project.Length + 1).Replace('\','/')
    })
    foreach ($path in $actualPaths) {
        if ($lockedPaths -cnotcontains $path) { $issues.Add("Untracked skill file: $path") }
    }
    foreach ($record in $lock.files) {
        if ($record.path -notmatch '^\.agents/skills/[a-z0-9-]+/' -or $record.path -match '\.\.|:|\\') {
            $issues.Add("Unsafe manifest path: $($record.path)"); continue
        }
        $file = Join-Path $project $record.path
        if (-not (Test-Path -LiteralPath $file -PathType Leaf)) { $issues.Add("Missing locked file: $($record.path)"); continue }
        if ((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash -ne $record.sha256) {
            $issues.Add("Changed locked file: $($record.path)")
        }
    }
}
if ($issues.Count) {
    $issues | ForEach-Object { Write-Output "FAIL: $_" }
    throw "$($issues.Count) skill validation issue(s)."
}
Write-Output "PASS: $($folders.Count) skill folders; scalar metadata, requested names, sections and local links valid."
if (-not $SkipHashes -and (Test-Path -LiteralPath $manifest)) { Write-Output 'PASS: skill file inventory and SHA256 hashes match.' }
Write-Output 'Native Codex discovery and gameplay behavior require separate verification.'
