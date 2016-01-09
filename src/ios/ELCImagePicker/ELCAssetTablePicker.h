//
//  ELCAssetTablePicker.h
//  HelloCordova
//
//  Created by zsly on 16/1/8.
//
//

#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "ELCAssetSelectionDelegate.h"
#import "ELCAssetPickerFilterDelegate.h"
@interface ELCAssetTablePicker : UIViewController
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIButton *preview;
@property (weak, nonatomic) IBOutlet UIButton *finish;
@property (weak, nonatomic) IBOutlet UILabel *count_label;
@property (weak, nonatomic) IBOutlet UIView *line;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *lineHeight;

@property (nonatomic, weak) id <ELCAssetSelectionDelegate> parent;
@property (nonatomic, strong) ALAssetsGroup *assetGroup;

// optional, can be used to filter the assets displayed
@property(nonatomic, weak) id<ELCAssetPickerFilterDelegate> assetPickerFilterDelegate;

@end
