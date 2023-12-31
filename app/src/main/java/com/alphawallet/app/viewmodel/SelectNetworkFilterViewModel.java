package com.alphawallet.app.viewmodel;

import android.widget.Toast;

import com.alphawallet.app.App;
import com.alphawallet.app.entity.NetworkInfo;
import com.alphawallet.app.repository.EthereumNetworkRepository;
import com.alphawallet.app.repository.EthereumNetworkRepositoryType;
import com.alphawallet.app.repository.PreferenceRepositoryType;
import com.alphawallet.app.service.AnalyticsServiceType;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.widget.entity.NetworkItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SelectNetworkFilterViewModel extends BaseViewModel {
    private final EthereumNetworkRepositoryType networkRepository;
    private final TokensService tokensService;
    private final PreferenceRepositoryType preferenceRepository;
    public int[] defaultnets = new int[] {1,56,61,137, 43114};

    public static boolean contains(int []array, int value) {
        for (int element : array)
            if (element == value)
                return true;
        return false;
    }

    @Inject
    public SelectNetworkFilterViewModel(EthereumNetworkRepositoryType ethereumNetworkRepositoryType,
                                        TokensService tokensService,
                                        PreferenceRepositoryType preferenceRepository,
                                        AnalyticsServiceType analyticsService) {
        this.networkRepository = ethereumNetworkRepositoryType;
        this.tokensService = tokensService;
        this.preferenceRepository = preferenceRepository;
        setAnalyticsService(analyticsService);
    }

    public NetworkInfo[] getNetworkList() {
        return networkRepository.getAvailableNetworkList();
    }

    public void setFilterNetworks(List<Long> selectedItems, boolean mainnetActive, boolean hasSelected, boolean shouldBlankUserSelection)
    {
        preferenceRepository.setActiveMainnet(mainnetActive);

        NetworkInfo activeNetwork = networkRepository.getActiveBrowserNetwork();
        long activeNetworkId = -99;
        if (activeNetwork != null) {
            activeNetworkId = networkRepository.getActiveBrowserNetwork().chainId;
        }

        //Mark dappbrowser network as deselected if appropriate
        boolean deselected = true;
        Long[] selectedIds = new Long[selectedItems.size()];
        int index = 0;
        for (Long selectedId : selectedItems) {
            if (EthereumNetworkRepository.hasRealValue(selectedId) == mainnetActive && activeNetworkId == selectedId) {
                deselected = false;
            }
            selectedIds[index++] = selectedId;
        }

        if (deselected) networkRepository.setActiveBrowserNetwork(null);
        networkRepository.setFilterNetworkList(selectedIds);
        tokensService.setupFilter(hasSelected && !shouldBlankUserSelection);

        if (shouldBlankUserSelection) preferenceRepository.blankHasSetNetworkFilters();

        preferenceRepository.commit();
    }

    public boolean hasShownTestNetWarning()
    {
        return preferenceRepository.hasShownTestNetWarning();
    }

    public void setShownTestNetWarning()
    {
        preferenceRepository.setShownTestNetWarning();
    }

    public NetworkInfo getNetworkByChain(long chainId)
    {
        return networkRepository.getNetworkByChain(chainId);
    }

    public boolean mainNetActive()
    {
        return preferenceRepository.isActiveMainnet();
    }

    public List<NetworkItem> getNetworkList(boolean isMainNet)
    {
        List<NetworkItem> networkList = new ArrayList<>();
        List<Long> filterIds = networkRepository.getSelectedFilters(isMainNet);
        for (NetworkInfo info : getNetworkList())
        {
            if (EthereumNetworkRepository.hasRealValue(info.chainId) == isMainNet)
            {
                networkList.add(new NetworkItem(info.name, info.chainId, filterIds.contains(info.chainId)));
            }
        }
        return networkList;
    }

    public List<NetworkItem> getNetworkList_showmore(boolean isMainNet, boolean isMoreShow)
    {
        List<NetworkItem> networkList = new ArrayList<>();
        List<Long> filterIds = networkRepository.getSelectedFilters(isMainNet);

        if (isMoreShow == true)
        {

            for (NetworkInfo info : getNetworkList())
            {
                //Binance=56, Ether=1, Ethereum Classic=61, Brise, Polygon =137, avalanche=43114, CRO, dogech
                if (EthereumNetworkRepository.hasRealValue(info.chainId) == isMainNet)
                {
                    networkList.add(new NetworkItem(info.name, info.chainId, filterIds.contains(info.chainId)));
                }
            }
        }
        else
        {
            for (NetworkInfo info : getNetworkList())
            {
                //Binance=56, Ether=1, Ethereum Classic=61, Brise, Polygon =137, avalanche=43114, CRO, dogech
                if (EthereumNetworkRepository.hasRealValue(info.chainId) == isMainNet && contains(defaultnets, (int)info.chainId))
                {
                    networkList.add(new NetworkItem(info.name, info.chainId, filterIds.contains(info.chainId)));
                }
            }
        }
        return networkList;
    }

    public void removeCustomNetwork(long chainId) {
        networkRepository.removeCustomRPCNetwork(chainId);
    }

    public TokensService getTokensService() {
        return tokensService;
    }

    public List<Long> getActiveNetworks()
    {
        return networkRepository.getFilterNetworkList();
    }

}
