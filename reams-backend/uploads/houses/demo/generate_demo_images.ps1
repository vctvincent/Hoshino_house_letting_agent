Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = 'Stop'
$outputDir = Join-Path $PSScriptRoot 'real'
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

function New-Scene {
    param([int]$Width = 1280, [int]$Height = 860)

    $bmp = New-Object System.Drawing.Bitmap $Width, $Height
    $graphics = [System.Drawing.Graphics]::FromImage($bmp)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    return @{ Bitmap = $bmp; Graphics = $graphics; Width = $Width; Height = $Height }
}

function Save-Scene {
    param(
        [hashtable]$Scene,
        [string]$Name
    )

    $path = Join-Path $outputDir $Name
    $Scene.Bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $Scene.Graphics.Dispose()
    $Scene.Bitmap.Dispose()
    Write-Output $path
}

function New-Brush {
    param([string]$Hex)
    return New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml($Hex))
}

function New-Pen {
    param([string]$Hex, [float]$Width = 1)
    return New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml($Hex), $Width)
}

function Fill-Gradient {
    param(
        $Graphics,
        [int]$X,
        [int]$Y,
        [int]$Width,
        [int]$Height,
        [string]$From,
        [string]$To,
        $Mode = 'Vertical'
    )
    $rect = New-Object System.Drawing.Rectangle $X, $Y, $Width, $Height
    if ($Mode -isnot [System.Drawing.Drawing2D.LinearGradientMode]) {
        $Mode = [System.Enum]::Parse([System.Drawing.Drawing2D.LinearGradientMode], [string]$Mode)
    }
    $brush = New-Object System.Drawing.Drawing2D.LinearGradientBrush (
        $rect,
        ([System.Drawing.ColorTranslator]::FromHtml($From)),
        ([System.Drawing.ColorTranslator]::FromHtml($To)),
        $Mode
    )
    $Graphics.FillRectangle($brush, $rect)
    $brush.Dispose()
}

function Draw-WindowView {
    param(
        $Graphics,
        [int]$X,
        [int]$Y,
        [int]$Width,
        [int]$Height,
        [string]$SkyTop = '#dcefff',
        [string]$SkyBottom = '#ffffff',
        [string]$TowerColor = '#9bb1c7',
        [switch]$WithWater
    )

    Fill-Gradient $Graphics $X $Y $Width $Height $SkyTop $SkyBottom

    $treeBrush = New-Brush '#739d78'
    $towerBrush = New-Brush $TowerColor
    $towerBrushDark = New-Brush '#6f859c'
    $waterBrush = New-Brush '#b7dbe8'

    if ($WithWater) {
        $Graphics.FillRectangle($waterBrush, $X, $Y + [int]($Height * 0.6), $Width, [int]($Height * 0.15))
    }

    $randHeights = @(0.42, 0.55, 0.48, 0.68, 0.5, 0.72, 0.58)
    for ($i = 0; $i -lt $randHeights.Length; $i++) {
        $w = 46 + ($i % 3) * 10
        $h = [int]($Height * $randHeights[$i])
        $left = $X + 40 + ($i * 92)
        $top = $Y + $Height - $h - 70
        $brush = if ($i % 2 -eq 0) { $towerBrush } else { $towerBrushDark }
        $Graphics.FillRectangle($brush, $left, $top, $w, $h)
    }

    for ($i = 0; $i -lt 6; $i++) {
        $radius = 26 + ($i % 3) * 8
        $cx = $X + 60 + ($i * 96)
        $cy = $Y + $Height - 65
        $Graphics.FillEllipse($treeBrush, $cx, $cy, $radius * 2, $radius * 2)
    }

    $treeBrush.Dispose()
    $towerBrush.Dispose()
    $towerBrushDark.Dispose()
    $waterBrush.Dispose()
}

function Draw-Floor {
    param($Graphics, [int]$Width, [int]$Height, [string]$From = '#d6b38d', [string]$To = '#b1845f')

    Fill-Gradient $Graphics 0 ([int]($Height * 0.58)) $Width ([int]($Height * 0.42)) $From $To 'Vertical'
    $pen = New-Pen '#c99970' 3
    for ($i = 0; $i -lt 11; $i++) {
        $y = [int]($Height * 0.61) + ($i * 26)
        $Graphics.DrawLine($pen, 0, $y, $Width, [int]($y + 12))
    }
    $pen.Dispose()
}

function Draw-WindowFrame {
    param($Graphics, [int]$X, [int]$Y, [int]$Width, [int]$Height)

    $framePen = New-Pen '#3f4853' 12
    $Graphics.DrawRectangle($framePen, $X, $Y, $Width, $Height)
    $Graphics.DrawLine($framePen, $X + [int]($Width * 0.5), $Y, $X + [int]($Width * 0.5), $Y + $Height)
    $Graphics.DrawLine($framePen, $X, $Y + [int]($Height * 0.55), $X + $Width, $Y + [int]($Height * 0.55))
    $framePen.Dispose()
}

function Draw-Sofa {
    param($Graphics, [int]$X, [int]$Y, [int]$Width, [int]$Height, [string]$Color = '#d9d2c6')

    $brush = New-Brush $Color
    $shadow = New-Brush '#c3b6a5'
    $Graphics.FillRectangle($shadow, $X + 18, $Y + $Height - 16, $Width - 36, 18)
    $Graphics.FillRoundedRectangle($brush, $X, $Y, $Width, $Height, 26)
    $Graphics.FillRoundedRectangle($brush, $X + 22, $Y - 32, [int]($Width * 0.32), 64, 20)
    $Graphics.FillRoundedRectangle($brush, $X + [int]($Width * 0.42), $Y - 22, [int]($Width * 0.24), 54, 20)
    $Graphics.FillRoundedRectangle($brush, $X + [int]($Width * 0.72), $Y - 32, [int]($Width * 0.22), 64, 20)
    $brush.Dispose()
    $shadow.Dispose()
}

function Draw-CoffeeTable {
    param($Graphics, [int]$X, [int]$Y, [int]$Width, [int]$Height)

    $topBrush = New-Brush '#ece7df'
    $legPen = New-Pen '#625746' 6
    $Graphics.FillEllipse($topBrush, $X, $Y, $Width, $Height)
    $Graphics.DrawLine($legPen, $X + 40, $Y + $Height - 6, $X + 30, $Y + $Height + 44)
    $Graphics.DrawLine($legPen, $X + $Width - 40, $Y + $Height - 6, $X + $Width - 28, $Y + $Height + 44)
    $topBrush.Dispose()
    $legPen.Dispose()
}

function Draw-Plant {
    param($Graphics, [int]$X, [int]$Y, [float]$Scale = 1)

    $potBrush = New-Brush '#c98f62'
    $leafBrush = New-Brush '#4f8d5b'
    $Graphics.FillEllipse($potBrush, $X, $Y + (58 * $Scale), 52 * $Scale, 22 * $Scale)
    $Graphics.FillRectangle($potBrush, $X + (6 * $Scale), $Y + (42 * $Scale), 40 * $Scale, 26 * $Scale)
    for ($i = 0; $i -lt 6; $i++) {
        $dx = (Get-Random -Minimum -18 -Maximum 18)
        $Graphics.FillEllipse($leafBrush, $X + 18 * $Scale + $dx, $Y + ($i * 12 * $Scale), 24 * $Scale, 54 * $Scale)
    }
    $potBrush.Dispose()
    $leafBrush.Dispose()
}

function Draw-Bed {
    param($Graphics, [int]$X, [int]$Y, [int]$Width, [int]$Height, [string]$Color = '#d5c3b0')

    $frameBrush = New-Brush '#8a6447'
    $matBrush = New-Brush '#f0ece7'
    $blanketBrush = New-Brush $Color
    $pillowBrush = New-Brush '#fbfaf7'
    $Graphics.FillRectangle($frameBrush, $X, $Y + 18, $Width, $Height)
    $Graphics.FillRectangle($frameBrush, $X - 14, $Y - 80, $Width + 28, 100)
    $Graphics.FillRectangle($matBrush, $X + 18, $Y + 28, $Width - 36, $Height - 30)
    $Graphics.FillRectangle($blanketBrush, $X + 18, $Y + 86, $Width - 36, $Height - 88)
    $Graphics.FillEllipse($pillowBrush, $X + 42, $Y + 36, 120, 56)
    $Graphics.FillEllipse($pillowBrush, $X + 180, $Y + 36, 120, 56)
    $frameBrush.Dispose()
    $matBrush.Dispose()
    $blanketBrush.Dispose()
    $pillowBrush.Dispose()
}

function Draw-DiningTable {
    param($Graphics, [int]$X, [int]$Y, [int]$Width, [int]$Height)
    $brush = New-Brush '#d1b18d'
    $legPen = New-Pen '#6d5a47' 6
    $Graphics.FillRoundedRectangle($brush, $X, $Y, $Width, $Height, 18)
    $Graphics.DrawLine($legPen, $X + 24, $Y + $Height, $X + 18, $Y + $Height + 58)
    $Graphics.DrawLine($legPen, $X + $Width - 24, $Y + $Height, $X + $Width - 18, $Y + $Height + 58)
    $brush.Dispose()
    $legPen.Dispose()
}

function Draw-Cabinets {
    param($Graphics, [int]$Width, [int]$Height)
    $cabBrush = New-Brush '#d9d0c3'
    $topBrush = New-Brush '#f6f3ed'
    $darkBrush = New-Brush '#5b6470'
    $Graphics.FillRectangle($cabBrush, 0, 120, $Width, 250)
    $Graphics.FillRectangle($topBrush, 0, 120, $Width, 40)
    $Graphics.FillRectangle($darkBrush, 0, 330, $Width, 24)
    for ($i = 0; $i -lt 6; $i++) {
        $Graphics.DrawRectangle((New-Pen '#b1a89c' 2), 50 + ($i * 160), 168, 120, 120)
    }
    $cabBrush.Dispose()
    $topBrush.Dispose()
    $darkBrush.Dispose()
}

function Draw-ExteriorSky {
    param($Graphics, [int]$Width, [int]$Height)
    Fill-Gradient $Graphics 0 0 $Width ([int]($Height * 0.68)) '#d8efff' '#fefefe'
    $sunBrush = New-Brush '#fff2c2'
    $Graphics.FillEllipse($sunBrush, $Width - 220, 60, 110, 110)
    $sunBrush.Dispose()
}

Update-TypeData -TypeName System.Drawing.Graphics -MemberType ScriptMethod -MemberName FillRoundedRectangle -Value {
    param($Brush, [float]$X, [float]$Y, [float]$Width, [float]$Height, [float]$Radius)
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $Radius * 2
    $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
    $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
    $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    $this.FillPath($Brush, $path)
    $path.Dispose()
} -Force

function Render-LivingRoom1 {
    $scene = New-Scene
    $g = $scene.Graphics
    Fill-Gradient $g 0 0 $scene.Width $scene.Height '#f5f2eb' '#ede7de'
    Draw-Floor $g $scene.Width $scene.Height '#d6b38d' '#b1845f'
    Draw-WindowView $g 760 120 420 360 '#dff1ff' '#ffffff' '#99aec1'
    Draw-WindowFrame $g 760 120 420 360
    $wallPen = New-Pen '#e8e0d3' 6
    $g.DrawLine($wallPen, 0, 500, 760, 420)
    $wallPen.Dispose()
    Draw-Sofa $g 170 470 360 150 '#d9d2c7'
    Draw-CoffeeTable $g 360 580 170 58
    Draw-Plant $g 1030 492 1.3
    $rugBrush = New-Brush '#d6cec1'
    $g.FillEllipse($rugBrush, 240, 560, 410, 150)
    $rugBrush.Dispose()
    $lampBrush = New-Brush '#d6cabd'
    $g.FillEllipse($lampBrush, 630, 240, 44, 44)
    $g.FillRectangle($lampBrush, 650, 180, 6, 90)
    $lampBrush.Dispose()
    Save-Scene $scene 'living-room-modern-01.png'
}

function Render-LivingRoom2 {
    $scene = New-Scene
    $g = $scene.Graphics
    Fill-Gradient $g 0 0 $scene.Width $scene.Height '#eef2f5' '#f8fbfc'
    Draw-Floor $g $scene.Width $scene.Height '#cfb090' '#a97d59'
    Draw-WindowView $g 720 100 470 400 '#d6ecff' '#ffffff' '#7b95b2' -WithWater
    Draw-WindowFrame $g 720 100 470 400
    Draw-Sofa $g 120 490 420 160 '#cfd4d8'
    Draw-CoffeeTable $g 360 590 180 60
    Draw-Plant $g 890 500 1.2
    $artBrush = New-Brush '#cfd8df'
    $g.FillRectangle($artBrush, 120, 120, 220, 160)
    $g.FillEllipse((New-Brush '#9ab0bd'), 180, 150, 120, 94)
    $artBrush.Dispose()
    Save-Scene $scene 'living-room-modern-02.png'
}

function Render-Loft {
    $scene = New-Scene
    $g = $scene.Graphics
    Fill-Gradient $g 0 0 $scene.Width $scene.Height '#f7f2ea' '#efe8dd'
    Draw-Floor $g $scene.Width $scene.Height '#d1ad88' '#a87d59'
    Draw-WindowView $g 760 100 420 430 '#dcefff' '#ffffff' '#95a9c2'
    Draw-WindowFrame $g 760 100 420 430
    $woodBrush = New-Brush '#a97b52'
    $g.FillPolygon($woodBrush, [System.Drawing.Point[]]@(
        (New-Object System.Drawing.Point -ArgumentList 0, 120),
        (New-Object System.Drawing.Point -ArgumentList 540, 80),
        (New-Object System.Drawing.Point -ArgumentList 540, 220),
        (New-Object System.Drawing.Point -ArgumentList 0, 250)
    ))
    $stairsPen = New-Pen '#666666' 5
    for ($i = 0; $i -lt 7; $i++) {
        $g.DrawLine($stairsPen, 130 + ($i * 24), 445 - ($i * 34), 270 + ($i * 24), 445 - ($i * 34))
    }
    $g.DrawLine($stairsPen, 170, 520, 390, 250)
    $g.DrawLine($stairsPen, 215, 550, 435, 280)
    $stairsPen.Dispose()
    Draw-Sofa $g 320 510 280 120 '#d9d0c8'
    Draw-CoffeeTable $g 520 588 150 50
    Draw-Plant $g 1040 532 1.1
    $woodBrush.Dispose()
    Save-Scene $scene 'loft-living-01.png'
}

function Render-FamilyLiving {
    $scene = New-Scene
    $g = $scene.Graphics
    Fill-Gradient $g 0 0 $scene.Width $scene.Height '#f6f3ed' '#ece7df'
    Draw-Floor $g $scene.Width $scene.Height '#d5b491' '#b68b68'
    Draw-WindowView $g 670 130 430 340 '#daf0ff' '#ffffff' '#8aa3b8'
    Draw-WindowFrame $g 670 130 430 340
    Draw-Sofa $g 160 500 390 150 '#e2d4bf'
    Draw-Sofa $g 470 540 260 112 '#d6cbc3'
    Draw-CoffeeTable $g 470 600 150 52
    Draw-Plant $g 1050 500 1.2
    $bookcaseBrush = New-Brush '#ccb293'
    $g.FillRectangle($bookcaseBrush, 80, 140, 140, 310)
    for ($i = 0; $i -lt 4; $i++) {
        $g.DrawLine((New-Pen '#a38463' 3), 85, 205 + ($i * 64), 215, 205 + ($i * 64))
    }
    $bookcaseBrush.Dispose()
    Save-Scene $scene 'family-living-01.png'
}

function Render-Bedroom1 {
    $scene = New-Scene
    $g = $scene.Graphics
    Fill-Gradient $g 0 0 $scene.Width $scene.Height '#f4efe7' '#ece6dc'
    Draw-Floor $g $scene.Width $scene.Height '#d7b694' '#ae825d'
    Draw-WindowView $g 760 115 400 350 '#dff2ff' '#ffffff' '#8ea6be'
    Draw-WindowFrame $g 760 115 400 350
    Draw-Bed $g 230 430 440 200 '#d8c5b2'
    $curtainBrush = New-Brush '#b6bec8'
    $g.FillRectangle($curtainBrush, 720, 90, 44, 420)
    $g.FillRectangle($curtainBrush, 1160, 90, 44, 420)
    $g.FillEllipse((New-Brush '#ded6ca'), 270, 650, 350, 80)
    $curtainBrush.Dispose()
    Draw-Plant $g 1000 515 1
    Save-Scene $scene 'bedroom-window-01.png'
}

function Render-Bedroom2 {
    $scene = New-Scene
    $g = $scene.Graphics
    Fill-Gradient $g 0 0 $scene.Width $scene.Height '#f2efe9' '#ece5db'
    Draw-Floor $g $scene.Width $scene.Height '#d2af8b' '#aa7d58'
    Draw-WindowView $g 810 110 330 360 '#d6edff' '#ffffff' '#9cb0c3'
    Draw-WindowFrame $g 810 110 330 360
    Draw-Bed $g 260 450 420 190 '#cfc8c0'
    $deskBrush = New-Brush '#9e744f'
    $g.FillRectangle($deskBrush, 90, 500, 150, 110)
    $g.FillRoundedRectangle((New-Brush '#c89c77'), 140, 600, 90, 22, 10)
    $g.FillRoundedRectangle((New-Brush '#b18d6d'), 870, 500, 140, 26, 12)
    $deskBrush.Dispose()
    Save-Scene $scene 'bedroom-window-02.png'
}

function Render-Kitchen {
    $scene = New-Scene
    $g = $scene.Graphics
    Fill-Gradient $g 0 0 $scene.Width $scene.Height '#f7f5f0' '#eeece5'
    Draw-Floor $g $scene.Width $scene.Height '#d6b28d' '#ad825d'
    Draw-Cabinets $g $scene.Width $scene.Height
    Draw-DiningTable $g 310 520 250 46
    $islandBrush = New-Brush '#f3efe9'
    $counterBrush = New-Brush '#8d979f'
    $g.FillRectangle($islandBrush, 760, 430, 250, 170)
    $g.FillRectangle($counterBrush, 730, 405, 310, 28)
    for ($i = 0; $i -lt 3; $i++) {
        $g.FillEllipse((New-Brush '#b78f66'), 800 + ($i * 84), 595, 40, 40)
    }
    $lightPen = New-Pen '#9b8b76' 4
    $g.DrawLine($lightPen, 890, 40, 890, 170)
    $g.DrawLine($lightPen, 1010, 40, 1010, 170)
    $g.FillEllipse((New-Brush '#f4d9a2'), 860, 170, 60, 60)
    $g.FillEllipse((New-Brush '#f4d9a2'), 980, 170, 60, 60)
    $islandBrush.Dispose()
    $counterBrush.Dispose()
    $lightPen.Dispose()
    Save-Scene $scene 'kitchen-dining-01.png'
}

function Render-StudyBalcony {
    $scene = New-Scene
    $g = $scene.Graphics
    Fill-Gradient $g 0 0 $scene.Width $scene.Height '#f4f5f2' '#ecefe8'
    Draw-Floor $g $scene.Width $scene.Height '#d0af8a' '#a97e58'
    Draw-WindowView $g 640 100 500 390 '#daf0ff' '#ffffff' '#8ba2b7'
    Draw-WindowFrame $g 640 100 500 390
    $deskBrush = New-Brush '#936d48'
    $shelfBrush = New-Brush '#c3b29b'
    $chairBrush = New-Brush '#9bb6c4'
    $g.FillRectangle($deskBrush, 120, 480, 260, 36)
    $g.FillRectangle($deskBrush, 150, 520, 22, 110)
    $g.FillRectangle($deskBrush, 330, 520, 22, 110)
    $g.FillRectangle($chairBrush, 210, 520, 110, 80)
    $g.FillRectangle($shelfBrush, 70, 120, 170, 280)
    for ($i = 0; $i -lt 4; $i++) {
        $g.DrawLine((New-Pen '#a18a71' 4), 80, 180 + ($i * 52), 230, 180 + ($i * 52))
    }
    Draw-Plant $g 930 520 1.1
    $deskBrush.Dispose()
    $shelfBrush.Dispose()
    $chairBrush.Dispose()
    Save-Scene $scene 'study-balcony-01.png'
}

function Render-TowerExterior {
    $scene = New-Scene
    $g = $scene.Graphics
    Draw-ExteriorSky $g $scene.Width $scene.Height
    $groundBrush = New-Brush '#dce8d9'
    $pathBrush = New-Brush '#d4c7b6'
    $towerBrush1 = New-Brush '#c3d3df'
    $towerBrush2 = New-Brush '#a8bfce'
    $shadowBrush = New-Brush '#7b8ea1'
    $g.FillRectangle($groundBrush, 0, 560, $scene.Width, 300)
    $g.FillPolygon($pathBrush, [System.Drawing.Point[]]@(
        (New-Object System.Drawing.Point -ArgumentList 520, 860),
        (New-Object System.Drawing.Point -ArgumentList 710, 860),
        (New-Object System.Drawing.Point -ArgumentList 780, 560),
        (New-Object System.Drawing.Point -ArgumentList 450, 560)
    ))
    $g.FillRectangle($towerBrush1, 180, 220, 170, 360)
    $g.FillRectangle($towerBrush2, 390, 140, 220, 440)
    $g.FillRectangle($shadowBrush, 640, 180, 185, 400)
    $g.FillRectangle($towerBrush2, 860, 250, 150, 330)
    for ($x = 0; $x -lt 4; $x++) {
        for ($y = 0; $y -lt 10; $y++) {
            $g.FillRectangle((New-Brush '#f1f7fb'), 205 + ($x * 34), 250 + ($y * 28), 18, 14)
            $g.FillRectangle((New-Brush '#f1f7fb'), 420 + ($x * 44), 180 + ($y * 30), 24, 16)
        }
    }
    for ($i = 0; $i -lt 8; $i++) {
        Draw-Plant $g (70 + ($i * 140)) 600 0.8
    }
    $groundBrush.Dispose()
    $pathBrush.Dispose()
    $towerBrush1.Dispose()
    $towerBrush2.Dispose()
    $shadowBrush.Dispose()
    Save-Scene $scene 'tower-exterior-01.png'
}

function Render-GardenExterior {
    $scene = New-Scene
    $g = $scene.Graphics
    Draw-ExteriorSky $g $scene.Width $scene.Height
    $lawnBrush = New-Brush '#d7ead3'
    $buildingBrush = New-Brush '#d7c5ae'
    $roofBrush = New-Brush '#a6825f'
    $windowBrush = New-Brush '#eef4f8'
    $g.FillRectangle($lawnBrush, 0, 570, $scene.Width, 290)
    $g.FillRectangle($buildingBrush, 130, 230, 260, 290)
    $g.FillRectangle($buildingBrush, 430, 190, 310, 330)
    $g.FillRectangle($buildingBrush, 780, 250, 250, 270)
    $g.FillRectangle($roofBrush, 120, 210, 280, 22)
    $g.FillRectangle($roofBrush, 420, 170, 330, 22)
    $g.FillRectangle($roofBrush, 770, 230, 270, 22)
    for ($i = 0; $i -lt 5; $i++) {
        for ($j = 0; $j -lt 4; $j++) {
            $g.FillRectangle($windowBrush, 160 + ($j * 52), 270 + ($i * 42), 28, 22)
            $g.FillRectangle($windowBrush, 470 + ($j * 60), 235 + ($i * 46), 34, 24)
            if ($j -lt 3) {
                $g.FillRectangle($windowBrush, 815 + ($j * 56), 295 + ($i * 40), 30, 22)
            }
        }
    }
    for ($i = 0; $i -lt 9; $i++) {
        Draw-Plant $g (40 + ($i * 130)) 630 0.82
    }
    $lawnBrush.Dispose()
    $buildingBrush.Dispose()
    $roofBrush.Dispose()
    $windowBrush.Dispose()
    Save-Scene $scene 'garden-exterior-01.png'
}

Render-LivingRoom1
Render-LivingRoom2
Render-Loft
Render-FamilyLiving
Render-Bedroom1
Render-Bedroom2
Render-Kitchen
Render-StudyBalcony
Render-TowerExterior
Render-GardenExterior
