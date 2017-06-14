Sub CreatePresentation()
    Dim FileName As String
    Dim ImageName As String
    Dim DefaultTradSize As Integer
    Dim DefaultSimpSize As Integer
    Dim DefaultPadding As Integer
    Dim FontName As String
    Dim TradBold As Boolean
    Dim SimpBold As Boolean
    Dim TradRed As Integer
    Dim TradBlue As Integer
    Dim TradGreen As Integer
    Dim SimpRed As Integer
    Dim SimpBlue As Integer
    Dim SimpGreen As Integer
    Dim ColumnNum As Integer
    
    ' PARAMETERS
    FileName = "Output.txt"
    ImageName = "home.png"
    DefaultTradSize = 200
    DefaultSimpSize = 200
    DefaultPadding = 0
    FontName = "SimSun"
    TradBold = True
    SimpBold = True
    TradRed = 0
    TradBlue = 0
    TradGreen = 0
    SimpRed = 0
    SimpBlue = 255
    SimpGreen = 0
    ColumnNum = 20
    
    ' **********Do not touch code below***********
    
    FileName = ActivePresentation.Path & "\" & FileName
    ImageName = ActivePresentation.Path & "\" & ImageName
    ' Dim FileNum As Integer
    Dim DataLineRaw As Variant
    Dim DataLineTrad As String
    Dim DataLineSimp As String
    Dim Length As Integer
    Dim Count As Integer
    Dim SlideCount As Integer
    Dim Width As Integer
    Dim Height As Integer
    ActivePresentation.PageSetup.SlideHeight = 600
    ActivePresentation.PageSetup.SlideWidth = 800
    Width = ActivePresentation.PageSetup.SlideWidth
    Height = ActivePresentation.PageSetup.SlideHeight
    Count = 0
    SlideCount = 2
    
    ' FileNum = FreeFile()
    ' Open FileName For Input As #FileNum
    
    Dim adoStream As ADODB.Stream
    Dim var_String As Variant
     
    Set adoStream = New ADODB.Stream
     
    adoStream.Charset = "UTF-8"
    adoStream.Open
    adoStream.LoadFromFile FileName 'change this to point to your text file
     
    var_String = Split(adoStream.ReadText, vbCrLf) 'split entire file into array - lines delimited by CRLF
    
    For Each DataLineRaw In var_String
        If Count Mod 2 = 0 Then
            DataLineTrad = CStr(DataLineRaw)
            Dim objRegex As Object
            Set objRegex = CreateObject("vbscript.regexp")
            With objRegex
                .Global = True
                .Pattern = "[\u0000-\u4E00]+"
                DataLineTrad = .Replace(DataLineTrad, vbNullString)
            End With
        End If
        If Count Mod 2 = 1 Then
            DataLineSimp = CStr(DataLineRaw)
            ' Dim objRegex As Object
            Set objRegex = CreateObject("vbscript.regexp")
            With objRegex
                .Global = True
                .Pattern = "[\u0000-\u4E00]+"
                DataLineSimp = .Replace(DataLineSimp, vbNullString)
            End With
            Length = Len(DataLineTrad)
            Dim Current As Slide
            Dim oLayout As CustomLayout
            Set oLayout = ActivePresentation.Designs(1).SlideMaster.CustomLayouts(1)
            Set Current = ActivePresentation.Slides.AddSlide(SlideCount, oLayout)
            Dim TradBox As Shape
            Set TradBox = Current.Shapes.AddTextbox(msoTextOrientationHorizontal, 0, 0, 1000, 0)
            TradBox.TextFrame.AutoSize = ppAutoSizeShapeToFitText
            TradBox.TextFrame.HorizontalAnchor = msoAnchorCenter
            TradBox.TextFrame.TextRange.Text = DataLineTrad
            Dim TradSize As Integer
            Dim SimpSize As Integer
            If Length < 4 Then
                TradSize = DefaultTradSize
                SimpSize = DefaultSimpSize
            Else
                TradSize = DefaultTradSize * 3 \ Length
                SimpSize = DefaultSimpSize * 3 \ Length
            End If
            With TradBox.TextFrame.TextRange.Font
                .Size = TradSize
                .Name = FontName
                .Bold = TradBold
                .Color.RGB = RGB(TradRed, TradGreen, TradBlue)
            End With
            If StrComp(DataLineSimp, DataLineTrad) = 0 Then
                With TradBox
                    ' .TextAlign = 2
                    .Left = Width / 2 - .Width / 2
                    .Top = Height / 2 - .Height / 2
                End With
            Else
                Dim SimpBox As Shape
                Set SimpBox = Current.Shapes.AddTextbox(msoTextOrientationHorizontal, 0, 0, 1000, 0)
                SimpBox.TextFrame.AutoSize = ppAutoSizeShapeToFitText
                SimpBox.TextFrame.HorizontalAnchor = msoAnchorCenter
                SimpBox.TextFrame.TextRange.Text = DataLineSimp
                With SimpBox.TextFrame.TextRange.Font
                    .Size = SimpSize
                    .Name = FontName
                    .Bold = SimpBold
                    .Color.RGB = RGB(SimpRed, SimpGreen, SimpBlue)
                End With
                With TradBox
                    ' .TextAlign = 2
                    .Left = Width / 2 - .Width / 2
                    .Top = Height / 2 - (.Height + SimpBox.Height + DefaultPadding) / 2
                End With
                With SimpBox
                    ' .TextAlign = 2
                    .Left = Width / 2 - .Width / 2
                    .Top = Height / 2 + DefaultPadding / 2
                End With
            End If
            Dim Image As Shape
            Set Image = Current.Shapes.AddPicture(ImageName, msoFalse, msoTrue, Width - 50, Height - 50, 30, 30)
            With Image.ActionSettings(ppMouseClick)
                .Action = ppActionHyperlink
                .Hyperlink.Address = ""
                .Hyperlink.SubAddress = 1
            End With
            SlideCount = SlideCount + 1
        End If
        Count = Count + 1
    Next
    
    ' Table of contents
    Dim TOC As Shape
    Set TOC = ActivePresentation.Slides(1).Shapes.AddTable((SlideCount - 2) \ ColumnNum + 1, ColumnNum, 10, 10, 300, 300)
    For i = 1 To (SlideCount - 2) \ ColumnNum + 1
        For j = 1 To ColumnNum
            k = (i - 1) * 20 + j
            If k < SlideCount - 1 Then
                TOC.Table.Cell(i, j).Shape.TextFrame.TextRange.Text = k
                With TOC.Table.Cell(i, j).Shape.TextFrame.TextRange.ActionSettings(ppMouseClick)
                    .Action = ppActionHyperlink
                    .Hyperlink.Address = ""
                    .Hyperlink.SubAddress = k + 1
                End With
            End If
        Next j
    Next i
End Sub


