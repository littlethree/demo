#import "RCTLeVideoPlayerViewController.h"

@interface RCTLeVideoPlayerViewController ()

@end

@implementation RCTLeVideoPlayerViewController

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [_rctDelegate videoPlayerViewControllerDidDismiss:self];
}

- (void)viewWillDisappear:(BOOL)animated {
    [_rctDelegate videoPlayerViewControllerWillDismiss:self];
    [super viewWillDisappear:animated];
}

@end
