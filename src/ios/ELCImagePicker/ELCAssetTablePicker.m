//
//  ELCAssetTablePicker.m
//  HelloCordova
//
//  Created by zsly on 16/1/8.
//
//

#import "ELCAssetTablePicker.h"
#import "ELCAsset.h"
#import "ELCAssetCell.h"
#import "ELCAlbumPickerController.h"
#import "ELCImagePickerController.h"
#import "MWPhotoBrowser.h"
@interface ELCAssetTablePicker ()<UITableViewDataSource,UITableViewDelegate,ELCAssetDelegate,MWPhotoBrowserDelegate>
@property (nonatomic, assign) NSInteger columns;
@property (nonatomic, strong) NSMutableArray *elcAssets;
@property (nonatomic, strong) NSMutableArray *photoArray;
@end


@implementation ELCAssetTablePicker

#pragma mark - MWPhotoBrowserDelegate
- (NSUInteger)numberOfPhotosInPhotoBrowser:(MWPhotoBrowser *)photoBrowser {
    return self.photoArray.count;
}

- (id <MWPhoto>)photoBrowser:(MWPhotoBrowser *)photoBrowser photoAtIndex:(NSUInteger)index {
    if (index < self.photoArray.count)
        return [self.photoArray objectAtIndex:index];
    return nil;
}

- (void)photoBrowserDidFinishModalPresentation:(MWPhotoBrowser *)photoBrowser {
    // If we subscribe to this method we must dismiss the view controller ourselves
    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - preview
-(void)preview:(id)sender
{
    [self.photoArray removeAllObjects];
    for (ELCAsset *elcAsset in self.elcAssets) {
        if ([elcAsset selected]) {
            ALAssetRepresentation* representation = [elcAsset.asset defaultRepresentation];
            NSString *file_path=representation.url.absoluteString;
            [self.photoArray addObject:[MWPhoto photoWithURL:[NSURL URLWithString:file_path]]];
        }
    }
    // Create browser
    MWPhotoBrowser *browser = [[MWPhotoBrowser alloc] initWithDelegate:self];
    browser.displayActionButton = NO;
    browser.displayNavArrows = NO;
    browser.displaySelectionButtons = NO;
    browser.alwaysShowControls = NO;
    browser.zoomPhotosToFill = YES;
    browser.enableGrid = NO;
    browser.startOnGrid = NO;
    browser.enableSwipeToDismiss = NO;
    [browser setCurrentPhotoIndex:0];
    [self.navigationController pushViewController:browser animated:YES];
}

#pragma mark - preparePhotos
- (void)preparePhotos
{
    @autoreleasepool {
        
        [self.assetGroup enumerateAssetsUsingBlock:^(ALAsset *result, NSUInteger index, BOOL *stop) {
            
            if (result == nil) {
                return;
            }
            
            ELCAsset *elcAsset = [[ELCAsset alloc] initWithAsset:result];
            [elcAsset setParent:self];
            
            BOOL isAssetFiltered = NO;
            if (self.assetPickerFilterDelegate &&
                [self.assetPickerFilterDelegate respondsToSelector:@selector(assetTablePicker:isAssetFilteredOut:)])
            {
                isAssetFiltered = [self.assetPickerFilterDelegate assetTablePicker:self isAssetFilteredOut:(ELCAsset*)elcAsset];
            }
            
            if (!isAssetFiltered) {
                [self.elcAssets addObject:elcAsset];
            }
            
        }];
        
        dispatch_sync(dispatch_get_main_queue(), ^{
            [self.tableView reloadData];
            // scroll to bottom
            long section = [self numberOfSectionsInTableView:self.tableView] - 1;
            long row = [self tableView:self.tableView numberOfRowsInSection:section] - 1;
            if (section >= 0 && row >= 0) {
                NSIndexPath *ip = [NSIndexPath indexPathForRow:row
                                                     inSection:section];
                [self.tableView scrollToRowAtIndexPath:ip
                                      atScrollPosition:UITableViewScrollPositionBottom
                                              animated:NO];
            }
            
        });
    }
}


#pragma mark - viewDidLoad
- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.columns = SCREEN_WIDTH / 80.0f;
    self.photoArray=[NSMutableArray array];
    [self BtnsEnable:NO];
    
    self.lineHeight.constant=1.0/SCREEN_SCALE;
    self.count_label.layer.cornerRadius = self.count_label.bounds.size.width/2.0f;
    self.count_label.clipsToBounds = YES;
    self.count_label.backgroundColor = [UIColor blackColor];
    self.count_label.textColor=[UIColor whiteColor];
    
    self.title=@"选择图片";
    [self.tableView setSeparatorStyle:UITableViewCellSeparatorStyleNone];
    [self.tableView setAllowsSelection:NO];
    
    NSMutableArray *tempArray = [[NSMutableArray alloc] init];
    self.elcAssets = tempArray;
    self.tableView.delegate=self;
    self.tableView.dataSource=self;
    
    UIBarButtonItem *cancelItem = [[UIBarButtonItem alloc] initWithTitle:@"取消" style:UIBarButtonItemStylePlain target:self action:@selector(cancel)];
    self.navigationItem.rightBarButtonItem = cancelItem;

    [self.finish addTarget:self action:@selector(finish:) forControlEvents:UIControlEventTouchUpInside];
    [self.preview addTarget:self action:@selector(preview:) forControlEvents:UIControlEventTouchUpInside];
    
    [self performSelectorInBackground:@selector(preparePhotos) withObject:nil];
    
    
    // Do any additional setup after loading the view.
}

-(void)BtnsEnable:(BOOL)enable
{
    if(self.finish.enabled!=enable)
    {
     self.finish.enabled=enable;
     self.preview.enabled=enable;
     self.count_label.hidden=enable?NO:YES;
    }
}

-(void)cancel
{
    //dismiss all
    ELCAlbumPickerController *vc=(ELCAlbumPickerController*)self.parent;
    ELCImagePickerController *parent=(ELCImagePickerController*)vc.parent;
    [parent cancelImagePicker];
}

- (void)finish:(id)sender
{
    NSMutableArray *selectedAssetsImages = [[NSMutableArray alloc] init];
    for (ELCAsset *elcAsset in self.elcAssets) {
        if ([elcAsset selected]) {
            [selectedAssetsImages addObject:[elcAsset asset]];
        }
    }
    [self.parent selectedAssets:selectedAssetsImages];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    return YES;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
    self.columns = SCREEN_WIDTH / 80.0f;
    [self.tableView reloadData];
}

#pragma mark - ELCAssetDelegate
- (BOOL)shouldSelectAsset:(ELCAsset *)asset
{
    NSUInteger selectionCount = 0;
    for (ELCAsset *elcAsset in self.elcAssets) {
        if (elcAsset.selected) selectionCount++;
    }
    BOOL shouldSelect = YES;
    if ([self.parent respondsToSelector:@selector(shouldSelectAsset:previousCount:)]) {
        shouldSelect = [self.parent shouldSelectAsset:asset previousCount:selectionCount];
    }
    return shouldSelect;
}

- (void)assetSelected:(ELCAsset *)asset
{
    [self BtnsEnable:YES];
     self.count_label.text=[NSString stringWithFormat:@"%ld",(long)[self totalSelectedAssets]];
}

- (void)assetdeSelected:(ELCAsset *)asset
{
    BOOL isEnable=[self totalSelectedAssets]==0?NO:YES;
    [self BtnsEnable:isEnable];
    self.count_label.text=[NSString stringWithFormat:@"%ld",(long)[self totalSelectedAssets]];
}

#pragma mark UITableViewDataSource Delegate Methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (self.columns <= 0) { //Sometimes called before we know how many columns we have
        self.columns = 4;
    }
    NSInteger numRows = ceil([self.elcAssets count] / (float)self.columns);
    return numRows;
}

- (NSArray *)assetsForIndexPath:(NSIndexPath *)path
{
    long index = path.row * self.columns;
    long length = MIN(self.columns, [self.elcAssets count] - index);
    return [self.elcAssets subarrayWithRange:NSMakeRange(index, length)];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
    
    ELCAssetCell *cell = (ELCAssetCell*)[tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    if (cell == nil) {
        cell = [[ELCAssetCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
    [cell setAssets:[self assetsForIndexPath:indexPath]];
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 79;
}


- (NSInteger)totalSelectedAssets
{
    NSInteger count = 0;
    for (ELCAsset *asset in self.elcAssets) {
        if (asset.selected) {
            count++;
        }
    }
    return count;
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
