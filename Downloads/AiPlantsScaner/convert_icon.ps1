# PowerShell Script to convert and resize app icon
# Usage: .\convert_icon.ps1

Add-Type -AssemblyName System.Drawing

$sourceImage = "C:\Users\User\Downloads\AiPlantsScaner\newicon.jpg"
$projectPath = "C:\Users\User\Downloads\AiPlantsScaner"

# Check if source image exists
if (-not (Test-Path $sourceImage)) {
    Write-Host "ERROR: Source image not found at $sourceImage" -ForegroundColor Red
    exit 1
}

Write-Host "Loading source image..." -ForegroundColor Green
$originalImage = [System.Drawing.Image]::FromFile($sourceImage)

Write-Host "Original size: $($originalImage.Width)x$($originalImage.Height)" -ForegroundColor Cyan

# Function to resize image
function Resize-Image {
    param (
        [System.Drawing.Image]$Image,
        [int]$Width,
        [int]$Height
    )
    
    $newImage = New-Object System.Drawing.Bitmap($Width, $Height)
    $graphics = [System.Drawing.Graphics]::FromImage($newImage)
    
    # High quality settings
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    
    $graphics.DrawImage($Image, 0, 0, $Width, $Height)
    $graphics.Dispose()
    
    return $newImage
}

# Define icon sizes for Android
$iconSizes = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

Write-Host "`nCreating icons..." -ForegroundColor Green

foreach ($folder in $iconSizes.Keys) {
    $size = $iconSizes[$folder]
    $folderPath = Join-Path $projectPath "app\src\main\res\$folder"
    
    # Create folder if it doesn't exist
    if (-not (Test-Path $folderPath)) {
        New-Item -ItemType Directory -Path $folderPath | Out-Null
    }
    
    Write-Host "  Creating ${size}x${size} icon for $folder..." -ForegroundColor Yellow
    
    # Resize and save ic_launcher.png
    $resizedImage = Resize-Image -Image $originalImage -Width $size -Height $size
    $outputPath = Join-Path $folderPath "ic_launcher.png"
    $resizedImage.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    # Also save as ic_launcher_round.png
    $outputPathRound = Join-Path $folderPath "ic_launcher_round.png"
    $resizedImage.Save($outputPathRound, [System.Drawing.Imaging.ImageFormat]::Png)
    
    $resizedImage.Dispose()
    
    Write-Host "    Saved: $outputPath" -ForegroundColor Gray
    Write-Host "    Saved: $outputPathRound" -ForegroundColor Gray
}

# Create high-res version for adaptive icon foreground (432x432)
Write-Host "`nCreating adaptive icon foreground (432x432)..." -ForegroundColor Green
$adaptivePath = Join-Path $projectPath "app\src\main\res\mipmap-xxxhdpi"
if (-not (Test-Path $adaptivePath)) {
    New-Item -ItemType Directory -Path $adaptivePath | Out-Null
}

$adaptiveImage = Resize-Image -Image $originalImage -Width 432 -Height 432
$adaptiveOutputPath = Join-Path $adaptivePath "ic_launcher_foreground.png"
$adaptiveImage.Save($adaptiveOutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
$adaptiveImage.Dispose()
Write-Host "  Saved: $adaptiveOutputPath" -ForegroundColor Gray

# Cleanup
$originalImage.Dispose()

Write-Host "`n✅ Icon conversion completed successfully!" -ForegroundColor Green
Write-Host "`nGenerated icons for:" -ForegroundColor Cyan
foreach ($folder in $iconSizes.Keys) {
    Write-Host "  - $folder (${iconSizes[$folder]}x${iconSizes[$folder]})" -ForegroundColor White
}

Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "1. Rebuild the project: .\gradlew.bat assembleDebug" -ForegroundColor White
Write-Host "2. Install on device: adb install -r app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
