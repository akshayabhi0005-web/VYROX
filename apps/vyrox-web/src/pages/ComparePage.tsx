import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Layers, Plus, X, Star, ArrowRight, Check } from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { ProductSummary } from '../types';

interface CompareMatrixData {
  products: ProductSummary[];
  groupedSpecs: Record<string, Record<string, string[]>>;
}

export const ComparePage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [matrix, setMatrix] = useState<CompareMatrixData | null>(null);
  const [availableProducts, setAvailableProducts] = useState<ProductSummary[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [loading, setLoading] = useState(true);

  // Extract selected IDs from search params or default to flagship items
  const productIds = [
    searchParams.get('p1'),
    searchParams.get('p2'),
    searchParams.get('p3'),
    searchParams.get('p4'),
  ].filter(Boolean).map(Number);

  const activeIds = productIds.length > 0 ? productIds : [1, 2, 3, 4];

  useEffect(() => {
    setLoading(true);

    apiClient.get('/products')
      .then((res) => {
        const list = res.data.content || res.data || [];
        setAvailableProducts(list);
      });

    apiClient.post('/products/compare', activeIds)
      .then((res) => setMatrix(res.data))
      .catch((err) => console.error('Failed to compare products', err))
      .finally(() => setLoading(false));
  }, [searchParams]);

  const addProductToCompare = (id: number) => {
    if (activeIds.includes(id)) return;
    if (activeIds.length >= 4) {
      alert('You can compare up to 4 products simultaneously.');
      return;
    }
    const updated = [...activeIds, id];
    const newParams = new URLSearchParams();
    updated.forEach((pid, idx) => newParams.set(`p${idx + 1}`, pid.toString()));
    setSearchParams(newParams);
    setShowAddModal(false);
  };

  const removeProductFromCompare = (id: number) => {
    const updated = activeIds.filter((pid) => pid !== id);
    const newParams = new URLSearchParams();
    updated.forEach((pid, idx) => newParams.set(`p${idx + 1}`, pid.toString()));
    setSearchParams(newParams);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-8">
      {/* Header */}
      <div className="bg-gradient-to-r from-[#0B192C] to-[#1E3E62] text-white p-6 sm:p-8 rounded-3xl shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-[#00D2FF] text-xs font-black tracking-widest uppercase mb-1">
            <Layers className="w-4 h-4" /> DYNAMIC COMPARISON MATRIX
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight">Compare Products Side-by-Side</h1>
          <p className="text-xs text-slate-300 mt-1">
            Analyze specs, prices, benchmarks, and ratings across up to 4 products dynamically.
          </p>
        </div>

        {activeIds.length < 4 && (
          <button
            onClick={() => setShowAddModal(true)}
            className="px-4 py-2.5 bg-[#FF6500] hover:bg-[#FF884B] text-white font-bold text-xs rounded-xl shadow-md flex items-center gap-1.5 self-start md:self-auto transition-all"
          >
            <Plus className="w-4 h-4" /> Add Product to Compare ({activeIds.length}/4)
          </button>
        )}
      </div>

      {loading ? (
        <div className="bg-white p-8 rounded-3xl border border-slate-200 text-center py-20 animate-pulse">
          <Layers className="w-10 h-10 text-slate-400 mx-auto mb-2 animate-bounce" />
          <p className="text-sm font-semibold text-slate-600">Building Comparison Matrix...</p>
        </div>
      ) : !matrix || matrix.products.length === 0 ? (
        <div className="bg-white p-8 rounded-3xl border border-slate-200 text-center py-20">
          <p className="text-sm font-semibold text-slate-600">No products selected for comparison.</p>
        </div>
      ) : (
        <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden overflow-x-auto">
          {/* Products Header Columns */}
          <div className="grid grid-flow-col auto-cols-[minmax(220px,1fr)] divide-x divide-slate-200 border-b border-slate-200 bg-slate-50/50 p-4">
            <div className="p-4 font-black text-slate-800 text-sm flex items-center">
              <div>
                <div className="text-[#2B6CB0] text-xs uppercase font-extrabold">Products</div>
                <div className="text-slate-500 text-xs font-normal">Side-by-side spec match</div>
              </div>
            </div>

            {matrix.products.map((prod) => (
              <div key={prod.id} className="p-4 flex flex-col justify-between relative group">
                {matrix.products.length > 2 && (
                  <button
                    onClick={() => removeProductFromCompare(prod.id)}
                    className="absolute top-2 right-2 p-1 text-slate-400 hover:text-rose-500 bg-white hover:bg-rose-50 rounded-full shadow-xs border border-slate-200 transition-all"
                    title="Remove from comparison"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                )}

                <div className="text-center space-y-2">
                  <img
                    src={prod.mainImageUrl}
                    alt={prod.title}
                    className="w-28 h-28 object-contain mx-auto mix-blend-multiply"
                  />
                  <div className="text-[11px] font-bold text-[#2B6CB0] uppercase">{prod.brandName}</div>
                  <Link
                    to={`/product/${prod.id}`}
                    className="text-xs font-bold text-slate-900 line-clamp-2 hover:text-[#2B6CB0] transition-colors"
                  >
                    {prod.title}
                  </Link>
                  <div className="text-base font-black text-slate-900">
                    ₹{prod.sellingPrice?.toLocaleString('en-IN')}
                  </div>
                  <div className="inline-flex items-center gap-1 bg-emerald-700 text-white text-[10px] font-bold px-2 py-0.5 rounded">
                    <span>{prod.averageRating?.toFixed(1)}</span>
                    <Star className="w-2.5 h-2.5 fill-current" />
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-200/80">
                  <Link
                    to={`/product/${prod.id}`}
                    className="w-full block text-center py-2 bg-[#0B192C] hover:bg-[#1E3E62] text-white text-xs font-bold rounded-xl transition-all shadow-xs"
                  >
                    View Details
                  </Link>
                </div>
              </div>
            ))}
          </div>

          {/* Grouped Specification Tables */}
          {Object.entries(matrix.groupedSpecs).map(([group, specs]) => (
            <div key={group} className="border-b border-slate-200">
              <div className="bg-slate-100/70 px-6 py-2.5 font-bold text-xs text-slate-800 uppercase tracking-wider border-y border-slate-200">
                {group}
              </div>
              <div className="divide-y divide-slate-100">
                {Object.entries(specs).map(([specName, values]) => (
                  <div key={specName} className="grid grid-flow-col auto-cols-[minmax(220px,1fr)] divide-x divide-slate-100 p-3 text-xs hover:bg-slate-50/70 transition-colors">
                    <div className="p-2 font-semibold text-slate-600">{specName}</div>
                    {values.map((val, idx) => (
                      <div key={idx} className="p-2 font-medium text-slate-900">
                        {val || '-'}
                      </div>
                    ))}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Product Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 max-w-lg w-full shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-base text-slate-900">Add Product to Compare</h3>
              <button
                onClick={() => setShowAddModal(false)}
                className="p-1 text-slate-400 hover:text-slate-600 rounded-full"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="max-h-96 overflow-y-auto space-y-2">
              {availableProducts
                .filter((p) => !activeIds.includes(p.id))
                .map((p) => (
                  <div
                    key={p.id}
                    className="flex items-center justify-between p-3 border border-slate-200 rounded-2xl hover:border-[#2B6CB0] transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <img src={p.mainImageUrl} alt={p.title} className="w-12 h-12 object-contain bg-slate-50 rounded-lg p-1" />
                      <div>
                        <div className="text-xs font-bold text-slate-900 line-clamp-1">{p.title}</div>
                        <div className="text-xs font-semibold text-[#2B6CB0]">₹{p.sellingPrice?.toLocaleString('en-IN')}</div>
                      </div>
                    </div>
                    <button
                      onClick={() => addProductToCompare(p.id)}
                      className="px-3 py-1.5 bg-[#0B192C] text-white text-xs font-bold rounded-xl hover:bg-[#1E3E62]"
                    >
                      Compare
                    </button>
                  </div>
                ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
