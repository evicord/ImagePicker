//
//  SOSPicker.m
//  SyncOnSet
//
//  Created by Christopher Sullivan on 10/25/13.
//
//

#import "SOSPicker.h"
#import "ELCAlbumPickerController.h"
#import "ELCImagePickerController.h"
#import "ELCAssetTablePicker.h"
#import "Operation.h"
#import "MBProgressHUD.h"

@interface SOSPicker()
@property (nonatomic,weak) MBProgressHUD *pro_hud;
@end

@implementation SOSPicker

@synthesize callbackId;

- (void) getPictures:(CDVInvokedUrlCommand *)command {
	NSDictionary *options = [command.arguments objectAtIndex: 0];

	NSInteger maximumImagesCount = [[options objectForKey:@"maximumImagesCount"] integerValue];
	self.width = [[options objectForKey:@"width"] integerValue];
	self.height = [[options objectForKey:@"height"] integerValue];
	self.quality = [[options objectForKey:@"quality"] integerValue];

	// Create the an album controller and image picker
	ELCAlbumPickerController *albumController = [[ELCAlbumPickerController alloc] init];
	
	if (maximumImagesCount == 1) {
      albumController.immediateReturn = true;
      albumController.singleSelection = true;
   } else {
      albumController.immediateReturn = false;
      albumController.singleSelection = false;
   }
   
   ELCImagePickerController *imagePicker = [[ELCImagePickerController alloc] initWithRootViewController:albumController];
   imagePicker.maximumImagesCount = maximumImagesCount;
   imagePicker.returnsOriginalImage = 1;
   imagePicker.imagePickerDelegate = self;

   albumController.parent = imagePicker;
	self.callbackId = command.callbackId;
	// Present modally
	[self.viewController presentViewController:imagePicker
	                       animated:YES
	                     completion:nil];
}

-(void)handleImage:(NSArray *)info
{
    CDVPluginResult*result=nil;
    NSMutableArray *resultStrings = [[NSMutableArray alloc] init];
    NSData* data = nil;
    NSError* err = nil;
    ALAsset* asset = nil;
    UIImageOrientation orientation = UIImageOrientationUp;
    NSString* tmpPath =[Operation tmpImagesDirectoryPath];
    NSString *filePath=nil;
    NSInteger i = 0;
    for (NSDictionary *dict in info) {
        asset = [dict objectForKey:@"ALAsset"];
        // From ELCImagePickerController.m
        
        NSDate *date=[NSDate date];
        filePath=[NSString stringWithFormat:@"%@/%ld_%@", tmpPath,(long)(date.timeIntervalSince1970+i),@"tmpImage"];
        i++;
        
        @autoreleasepool {
            ALAssetRepresentation *assetRep = [asset defaultRepresentation];
            CGImageRef imgRef = [assetRep fullResolutionImage];
            
            //defaultRepresentation returns image as it appears in photo picker, rotated and sized,
            //so use UIImageOrientationUp when creating our image below.
           
            
            UIImage* image = [UIImage imageWithCGImage:imgRef scale:1.0f orientation:orientation];
            UIImageOrientation imageOrientation=image.imageOrientation;
            if(imageOrientation!=UIImageOrientationUp)
            {
                UIGraphicsBeginImageContext(image.size);
                [image drawInRect:CGRectMake(0, 0, image.size.width, image.size.height)];
                image = UIGraphicsGetImageFromCurrentImageContext();
                UIGraphicsEndImageContext();
            }
            
            CGFloat width=image.size.width;
            CGFloat height=image.size.height;
            if(width>height)
            {
                height=image.size.height>=2048?2048:image.size.height;
            }
            else
            {
                width=image.size.width>=2048?2048:image.size.width;
            }
            
            CGSize fit_size=CGSizeMake(width, height);
            image=[Operation scaleToSize:image size:fit_size];
            data=UIImageJPEGRepresentation(image, 0.9f);
            
            if (![data writeToFile:filePath options:NSAtomicWrite error:&err]) {
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_IO_EXCEPTION messageAsString:[err localizedDescription]];
                break;
            } else {
                NSDictionary *dict=@{@"path":[[NSURL fileURLWithPath:filePath] absoluteString],@"width":@(fit_size.width),@"height":@(fit_size.height)};
                [resultStrings addObject:dict];
                /*ios 要修改相册库,不支持先dismiss再获取相册资源
                 NSDictionary *handle_dict=@{@"type":@(1),@"result":dict,@"index":@(i)};
                 CDVPluginResult *handle = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:handle_dict];*/
                
                BOOL isFinish=NO;
                if(i==info.count)
                {
                    isFinish=YES;
                }
                CGFloat temp=(CGFloat)i;
                CGFloat temp_count=(CGFloat)info.count;
                CGFloat per=temp/temp_count;
                [self performSelectorOnMainThread:@selector(updatePro:) withObject:@{@"isFinish":@(isFinish),@"per":@(per)} waitUntilDone:YES];
            }
        }
    }
    if(result==nil)
    {
        NSDictionary *dict=@{@"type":@(0),@"result":resultStrings};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dict];
    }
    [self performSelectorOnMainThread:@selector(SendMsg:) withObject:result waitUntilDone:YES];
}

-(void)updatePro:(NSDictionary*)dict
{
    NSNumber *number=[dict objectForKey:@"per"];
    NSNumber *isFinish=[dict objectForKey:@"isFinish"];
    self.pro_hud.progress = number.floatValue;
    if(isFinish.boolValue)
    {
       [self.pro_hud hide:YES];
       [self.viewController dismissViewControllerAnimated:YES completion:nil];
    }
}

-(void)SendMsg:(CDVPluginResult*)handle
{
    [self.commandDelegate sendPluginResult:handle callbackId:self.callbackId];
}

- (void)elcImagePickerController:(ELCImagePickerController *)picker didFinishPickingMediaWithInfo:(NSArray *)info
{
    MBProgressHUD *hud = [self showProgressHUDAddToView:picker.view withLabelText:@"正在加载ing"];
    self.pro_hud=hud;
    NSThread *thread = [[NSThread alloc]initWithTarget:self selector:@selector(handleImage:) object:info];
    [thread start];
}

-(MBProgressHUD*)showProgressHUDAddToView:(UIView*)view withLabelText:(NSString *)text{
    return [self showHUDAddToView:view withLabelText:text detailText:nil dimBackground:NO animate:YES mode:MBProgressHUDModeDeterminate];
}

-(MBProgressHUD*)showHUDAddToView:(UIView*)view withLabelText:(NSString *)text detailText:(NSString*)detailText dimBackground:(BOOL)dimBackground animate:(BOOL)animate mode:(MBProgressHUDMode)mode{
    
    MBProgressHUD *hud = [MBProgressHUD HUDForView:view];
    if (hud == nil) {
        hud = [MBProgressHUD showHUDAddedTo:view animated:animate];
    }
    hud.mode = mode;
    hud.labelText = text;
    hud.detailsLabelText = detailText;
    hud.dimBackground = dimBackground;
    return hud;
}


- (void)elcImagePickerControllerDidCancel:(ELCImagePickerController *)picker {
	[self.viewController dismissViewControllerAnimated:YES completion:nil];
	CDVPluginResult* pluginResult = nil;
    NSArray* emptyArray = [NSArray array];
	pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:emptyArray];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (UIImage*)imageByScalingNotCroppingForSize:(UIImage*)anImage toSize:(CGSize)frameSize
{
    UIImage* sourceImage = anImage;
    UIImage* newImage = nil;
    CGSize imageSize = sourceImage.size;
    CGFloat width = imageSize.width;
    CGFloat height = imageSize.height;
    CGFloat targetWidth = frameSize.width;
    CGFloat targetHeight = frameSize.height;
    CGFloat scaleFactor = 0.0;
    CGSize scaledSize = frameSize;

    if (CGSizeEqualToSize(imageSize, frameSize) == NO) {
        CGFloat widthFactor = targetWidth / width;
        CGFloat heightFactor = targetHeight / height;

        // opposite comparison to imageByScalingAndCroppingForSize in order to contain the image within the given bounds
        if (widthFactor == 0.0) {
            scaleFactor = heightFactor;
        } else if (heightFactor == 0.0) {
            scaleFactor = widthFactor;
        } else if (widthFactor > heightFactor) {
            scaleFactor = heightFactor; // scale to fit height
        } else {
            scaleFactor = widthFactor; // scale to fit width
        }
        scaledSize = CGSizeMake(width * scaleFactor, height * scaleFactor);
    }

    UIGraphicsBeginImageContext(scaledSize); // this will resize

    [sourceImage drawInRect:CGRectMake(0, 0, scaledSize.width, scaledSize.height)];

    newImage = UIGraphicsGetImageFromCurrentImageContext();
    if (newImage == nil) {
        NSLog(@"could not scale image");
    }

    // pop the context to get back to the default
    UIGraphicsEndImageContext();
    return newImage;
}

@end
