// 장바구니 관리 유틸리티 (localStorage 기반)

export interface CartItem {
    productId: string
    productName: string
    productSlug: string
    seller: string
    price: number
    quantity: number
    thumbnailUrl?: string
}

const CART_KEY = 'ecommerce_cart'

export const getCart = (): CartItem[] => {
    try {
        const data = localStorage.getItem(CART_KEY)
        return data ? JSON.parse(data) : []
    } catch (e) {
        console.error('Failed to load cart:', e)
        return []
    }
}

export const saveCart = (items: CartItem[]): void => {
    try {
        localStorage.setItem(CART_KEY, JSON.stringify(items))
    } catch (e) {
        console.error('Failed to save cart:', e)
    }
}

export const addToCart = (item: CartItem): void => {
    let cart = getCart()

    // Check if cart has items from different seller
    if (cart.length > 0 && cart[0].seller !== item.seller) {
        console.warn(`Cart seller conflict: existing="${cart[0].seller}", new="${item.seller}"`)
        console.warn('UI should prevent this! Clearing cart as fallback.')
        clearCart()
        cart = [] // Reset to empty
    }

    const existingIndex = cart.findIndex(i => i.productId === item.productId)

    if (existingIndex >= 0) {
        // Same product exists - increase quantity
        cart[existingIndex].quantity += item.quantity
    } else {
        // Add new product
        cart.push(item)
    }

    saveCart(cart)
    console.log('Cart saved:', cart)
}

export const clearCart = (): void => {
    saveCart([])
}

export const getCartSeller = (): string | null => {
    const cart = getCart()
    return cart.length > 0 ? cart[0].seller : null
}

export const getCartItemCount = (): number => {
    const cart = getCart()
    return cart.reduce((sum, item) => sum + item.quantity, 0)
}

export const removeFromCart = (productId: string): void => {
    const cart = getCart()
    const filtered = cart.filter(item => item.productId !== productId)
    saveCart(filtered)
}
