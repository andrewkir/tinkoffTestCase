package ru.andrewkir.tinkofftestcase.flows.main

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable.Orientation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.andrewkir.tinkofftestcase.App
import ru.andrewkir.tinkofftestcase.MainActivity
import ru.andrewkir.tinkofftestcase.R
import ru.andrewkir.tinkofftestcase.common.BaseFragment
import ru.andrewkir.tinkofftestcase.common.ViewModelFactory
import ru.andrewkir.tinkofftestcase.databinding.FragmentPopularBinding
import ru.andrewkir.tinkofftestcase.flows.details.DetailsFragmentArgs
import ru.andrewkir.tinkofftestcase.flows.main.adapters.MoviesAdapter
import javax.inject.Inject

class PopularFragment : BaseFragment<PopularViewModel, FragmentPopularBinding>() {

    lateinit var adapter: MoviesAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun provideViewModel(): PopularViewModel {
        (requireContext().applicationContext as App).appComponent.inject(this)
        return ViewModelProvider(this, viewModelFactory)[PopularViewModel::class.java]
    }

    override fun provideBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPopularBinding = FragmentPopularBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val favouritesList = viewModel.getFavouritesList()

        (activity as MainActivity).flag = true

        adapter = MoviesAdapter({
            val action = PopularFragmentDirections.actionMainFragmentToDetailsFragment(it?.id ?: 0)
            if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
                findNavController().navigate(action)
            } else {
                (activity as MainActivity).navInLandscape(it?.id ?: 0)
            }
        }, {
            if (favouritesList.find { movieModel -> movieModel.id == it?.id } != null) {
                viewModel.removeItem(it)
            } else viewModel.addItem(it)
        })
        adapter.favouritesList = favouritesList

        bind.recyclerView.adapter = adapter
        bind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        bind.textView.text = "Популярное"

        subscribeToList()
    }

    private fun subscribeToList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getData()?.collectLatest { movies ->
                bind.progressBar.visibility = View.GONE
                adapter.submitData(movies)
            }
        }
    }
}