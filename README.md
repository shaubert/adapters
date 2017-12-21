# Adapters
Everyday use android adapters

##Gradle

    repositories {
        maven{url "https://github.com/shaubert/maven-repo/raw/master/releases"}
    }
    dependencies {
        compile 'com.shaubert.ui.adapters:library:1.2.14'
    }

## List of Adapters

From CommonsWare with changes:
  *  `AdapterWrapper` — to wrap adapters;
  *  `EndlessAdapter` — to load more items;
  *  `MergeAdapter` — to combine adapters;
  *  `SackOfViewsAdapter` — to create adapter from views;      
  
From library:  
  *  `StableIdsFragmentStatePagerAdapter` — FragmentStatePagerAdapter with stable item ids and notifyDataSetChange() support;
  *  `AdaptersCarousel` —  multi-adapter, you can populate it with other adapters and dynamically change current;
  *  `AdapterWithEmptyItem` — adapter wrapper that can show empty item if count == 0.
  *  `CheckableAdapter` — check/uncheck item views (that extends `Checkable` interface) in adapter;
  *  `FilteredAdapter` — adapter wrapper to filter items;
  *  `ListAdapter` — base list adapter, that supports filtering, comparison, and useful methods like `addAll()` and `replaceAll()`.
  *  `ListBaseAdapter` — `BaseAdapter` with list of items;
  *  `PagesAdapter` — simple `FragmentStatePagerAdapter` extension;
  *  `SectionListAdapter` — `BaseAdapter` with sections;
  
Versions for `RecycleView`:
  *  `RecyclerMergeAdapter` — same as `MergeAdapter`;
  *  `RecyclerAdapterWithEmptyItem` — same as `AdapterWithEmptyItem`;
  *  `RecyclerAdapterWrapper` — same as `AdapterWrapper`;
  *  `RecyclerEndlessAdapter` — same as `EndlessAdapter`;
  *  `RecyclerAdapter` — same as `ListBaseAdapter`;
  *  `RecyclerSackOfViewsAdapter` — same as `SackOfViewsAdapter`;
  *  `RecyclerSectionAdapter` — same as `SectionListAdapter`
  *  `RecyclerCheckableAdapter` — same as `CheckableAdapter`  
  
## Other
  *  `RapidMenuChangePageListener` — implementation of `ViewPager.OnPageChangeListener` to hide/show fragment menu on `onPageSelected` (faster than default way).
  
 